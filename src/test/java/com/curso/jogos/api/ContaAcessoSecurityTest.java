package com.curso.jogos.api;

import com.curso.jogos.application.ContaAcessoService;
import com.curso.jogos.domain.ContaAcesso;
import com.curso.jogos.domain.Pessoa;
import com.curso.jogos.repository.ContaAcessoRepository;
import com.curso.jogos.repository.PessoaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.jayway.jsonpath.JsonPath;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContaAcessoSecurityTest {
    private static final String SENHA = "uma-frase-senha-2026";

    @Autowired MockMvc mockMvc;
    @Autowired ContaAcessoService contaService;
    @Autowired ContaAcessoRepository contaRepository;
    @Autowired PessoaRepository pessoaRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired JwtDecoder jwtDecoder;

    @AfterEach
    void limparDados() {
        jdbcTemplate.update("DELETE FROM conta_acesso_perfil");
        jdbcTemplate.update("DELETE FROM conta_acesso");
        jdbcTemplate.update("DELETE FROM pessoa WHERE email LIKE '%@security.test'");
    }

    @Test
    void healthDevePermanecerPublico() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void jogosDeveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/jogos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroPublicoDeContaNaoDeveExporSenha() throws Exception {
        Pessoa pessoa = criarPessoa("cadastro");

        mockMvc.perform(post("/api/contas-acesso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pessoaId": %d,
                                  "login": "cadastro.2026",
                                  "senha": "%s"
                                }
                                """.formatted(pessoa.getId(), SENHA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("cadastro.2026"))
                .andExpect(jsonPath("$.perfis[0]").value("OPERADOR"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void loginValidoDeveEmitirJwtEPermitirConsultarPropriaConta() throws Exception {
        criarConta("operadora.2026");
        String token = autenticar("operadora.2026", SENHA);
        Jwt jwt = jwtDecoder.decode(token);

        assertEquals("https://jogos2026.local", jwt.getIssuer().toString());
        assertEquals("operadora.2026", jwt.getSubject());
        assertNotNull(jwt.getId());
        assertTrue(jwt.getClaimAsStringList("perfis").contains("OPERADOR"));
        assertTrue(jwt.getExpiresAt().isAfter(jwt.getIssuedAt()));

        mockMvc.perform(get("/api/contas-acesso/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("operadora.2026"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void senhaIncorretaNoLoginDeveRetornar401() throws Exception {
        criarConta("senha.errada");

        mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "senha.errada",
                                  "senha": "senha-incorreta-2026"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void contaBloqueadaNaoDeveReceberToken() throws Exception {
        ContaAcesso conta = criarConta("conta.bloqueada");
        conta.bloquear();
        contaRepository.save(conta);

        mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "conta.bloqueada",
                                  "senha": "%s"
                                }
                                """.formatted(SENHA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenMalformadoDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/contas-acesso/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.alterado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void basicNaoDeveMaisLiberarRotaProtegida() throws Exception {
        criarConta("basic.desativado");

        mockMvc.perform(get("/api/contas-acesso/me")
                        .with(httpBasic("basic.desativado", SENHA)))
                .andExpect(status().isUnauthorized());
    }

    private String autenticar(String login, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(login, senha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andReturn();

        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.accessToken");
    }

    private ContaAcesso criarConta(String login) {
        Pessoa pessoa = criarPessoa(login.replace('.', '-'));
        return contaService.cadastrar(pessoa.getId(), login, SENHA);
    }

    private Pessoa criarPessoa(String prefixo) {
        long semente = System.nanoTime();
        return pessoaRepository.save(new Pessoa(
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
