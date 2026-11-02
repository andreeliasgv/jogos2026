package com.curso.jogos.api;

import com.curso.jogos.domain.ContaAcesso;
import com.curso.jogos.domain.PerfilAcesso;
import com.curso.jogos.domain.Pessoa;
import com.curso.jogos.domain.StatusContaAcesso;
import com.curso.jogos.repository.ContaAcessoRepository;
import com.curso.jogos.repository.PessoaRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ContaAcessoSecurityTest {
    private static final String SENHA = "uma-frase-senha-2026";

    @Autowired MockMvc mockMvc;
    @Autowired ContaAcessoRepository contaRepository;
    @Autowired PessoaRepository pessoaRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtDecoder jwtDecoder;

    @Test
    void healthDevePermanecerPublico() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void rotaProtegidaSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/jogos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveDiferenciarAusenciaDeTokenDeOperadorSemPermissao() throws Exception {
        Pessoa pessoa = criarPessoa("matriz-acesso");
        String corpo = corpoCadastro(pessoa.getId(), "nova.conta");

        mockMvc.perform(post("/api/contas-acesso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnauthorized());

        salvarConta("operador.limitado", SENHA);
        String tokenOperador = autenticar("operador.limitado", SENHA);
        mockMvc.perform(post("/api/contas-acesso")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorDeveCadastrarContaSemExporSenha() throws Exception {
        ContaAcesso administrador = salvarConta(
                "administrador.cadastro", SENHA, PerfilAcesso.ADMINISTRADOR);
        String tokenAdministrador = autenticar(administrador.getLogin(), SENHA);
        Pessoa pessoa = criarPessoa("cadastro-admin");

        mockMvc.perform(post("/api/contas-acesso")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCadastro(pessoa.getId(), "cadastro.2026")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("cadastro.2026"))
                .andExpect(jsonPath("$.perfis[0]").value("OPERADOR"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        ContaAcesso criada = contaRepository.findByLoginIgnoreCase("cadastro.2026")
                .orElseThrow();
        assertNotEquals(SENHA, criada.getSenhaHash());
        assertTrue(passwordEncoder.matches(SENHA, criada.getSenhaHash()));
    }

    @Test
    void administradorDeveAlterarPerfisEBloquearOutraConta() throws Exception {
        ContaAcesso administrador = salvarConta(
                "administrador.gestao", SENHA, PerfilAcesso.ADMINISTRADOR);
        ContaAcesso operador = salvarConta("operador.gerenciado", SENHA);
        String tokenAdministrador = autenticar(administrador.getLogin(), SENHA);

        mockMvc.perform(put("/api/contas-acesso/{id}/perfis", operador.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"perfis":["OPERADOR","ADMINISTRADOR"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfis.length()").value(2));

        mockMvc.perform(put("/api/contas-acesso/{id}/status", operador.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"BLOQUEADA"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOQUEADA"));

        assertEquals(StatusContaAcesso.BLOQUEADA,
                contaRepository.findById(operador.getId()).orElseThrow().getStatus());
    }

    @Test
    void administradorNaoDeveBloquearAPropriaConta() throws Exception {
        ContaAcesso administrador = salvarConta(
                "administrador.protegido", SENHA, PerfilAcesso.ADMINISTRADOR);
        String tokenAdministrador = autenticar(administrador.getLogin(), SENHA);

        mockMvc.perform(put("/api/contas-acesso/{id}/status", administrador.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"BLOQUEADA"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "O administrador não pode bloquear a própria conta"));
    }

    @Test
    void loginValidoDeveEmitirJwtComAuthoritiesEPermitirOperacaoCotidiana() throws Exception {
        salvarConta("operadora.2026", SENHA);
        String token = autenticar("operadora.2026", SENHA);
        Jwt jwt = jwtDecoder.decode(token);

        assertEquals("https://jogos2026.local", jwt.getIssuer().toString());
        assertEquals("operadora.2026", jwt.getSubject());
        assertNotNull(jwt.getId());
        assertTrue(jwt.getClaimAsStringList("perfis").contains("OPERADOR"));

        mockMvc.perform(get("/api/contas-acesso/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("operadora.2026"));
    }

    @Test
    void senhaIncorretaEContaBloqueadaNaoDevemReceberToken() throws Exception {
        salvarConta("senha.errada", SENHA);
        ContaAcesso bloqueada = salvarConta("conta.bloqueada", SENHA);
        bloqueada.bloquear();
        contaRepository.saveAndFlush(bloqueada);

        mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"senha.errada","senha":"senha-incorreta-2026"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"conta.bloqueada","senha":"%s"}
                                """.formatted(SENHA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenMalformadoEBasicDevemRetornar401() throws Exception {
        salvarConta("basic.desativado", SENHA);

        mockMvc.perform(get("/api/contas-acesso/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.alterado"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/contas-acesso/me")
                        .with(httpBasic("basic.desativado", SENHA)))
                .andExpect(status().isUnauthorized());
    }

    private String autenticar(String login, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"%s","senha":"%s"}
                                """.formatted(login, senha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(resposta, "$.accessToken");
    }

    private String corpoCadastro(Long pessoaId, String login) {
        return """
                {"pessoaId":%d,"login":"%s","senha":"%s"}
                """.formatted(pessoaId, login, SENHA);
    }

    private ContaAcesso salvarConta(
            String login,
            String senha,
            PerfilAcesso... perfisAdicionais) {
        Pessoa pessoa = criarPessoa(login.replace('.', '-'));
        ContaAcesso conta = new ContaAcesso(
                pessoa,
                login,
                passwordEncoder.encode(senha));
        for (PerfilAcesso perfil : perfisAdicionais) {
            conta.concederPerfil(perfil);
        }
        return contaRepository.saveAndFlush(conta);
    }

    private Pessoa criarPessoa(String prefixo) {
        long semente = System.nanoTime();
        return pessoaRepository.saveAndFlush(new Pessoa(
                "Pessoa " + prefixo,
                prefixo + semente + "@security.test",
                gerarCpf(semente),
                LocalDate.now()));
    }

    private String gerarCpf(long semente) {
        String base = "%09d".formatted(Math.floorMod(semente, 1_000_000_000L));
        if (base.matches("(.)\\1{8}")) base = "123456789";
        int primeiro = digitoCpf(base, 10);
        return base + primeiro + digitoCpf(base + primeiro, 11);
    }

    private int digitoCpf(String base, int peso) {
        int soma = 0;
        for (char caractere : base.toCharArray()) {
            soma += Character.getNumericValue(caractere) * peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
