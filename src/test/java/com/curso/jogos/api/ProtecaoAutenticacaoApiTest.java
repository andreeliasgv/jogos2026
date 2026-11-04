package com.curso.jogos.api;

import com.curso.jogos.domain.ContaAcesso;
import com.curso.jogos.domain.PerfilAcesso;
import com.curso.jogos.domain.Pessoa;
import com.curso.jogos.domain.TipoEventoSeguranca;
import com.curso.jogos.repository.ContaAcessoRepository;
import com.curso.jogos.repository.EventoSegurancaRepository;
import com.curso.jogos.repository.PessoaRepository;
import com.curso.jogos.repository.ProtecaoAutenticacaoRepository;
import com.curso.jogos.repository.SessaoRefreshTokenRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProtecaoAutenticacaoApiTest {
    private static final String SENHA = "senha-auditoria-2026";

    @Autowired MockMvc mockMvc;
    @Autowired PessoaRepository pessoaRepository;
    @Autowired ContaAcessoRepository contaRepository;
    @Autowired SessaoRefreshTokenRepository sessaoRepository;
    @Autowired ProtecaoAutenticacaoRepository protecaoRepository;
    @Autowired EventoSegurancaRepository eventoRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private final List<ContaAcesso> contasCriadas = new ArrayList<>();
    private final List<Pessoa> pessoasCriadas = new ArrayList<>();

    @BeforeEach
    void removerEventosDeOutrosCenarios() {
        eventoRepository.deleteAll();
        protecaoRepository.deleteAll();
    }

    @AfterEach
    void limparCenario() {
        eventoRepository.deleteAll();
        protecaoRepository.deleteAll();
        sessaoRepository.deleteAll();
        contasCriadas.forEach(conta -> contaRepository.deleteById(conta.getId()));
        pessoasCriadas.forEach(pessoa -> pessoaRepository.deleteById(pessoa.getId()));
        contasCriadas.clear();
        pessoasCriadas.clear();
    }

    @Test
    void deveBloquearTemporariamenteDepoisDeCincoFalhas() throws Exception {
        criarConta("login.protegido", PerfilAcesso.OPERADOR);
        for (int tentativa = 1; tentativa <= 5; tentativa++) {
            loginEsperandoNaoAutorizado("login.protegido", "senha-incorreta");
        }
        loginEsperandoNaoAutorizado("login.protegido", SENHA);

        var protecao = protecaoRepository.findAll().getFirst();
        assertEquals(5, protecao.getFalhasConsecutivas());
        assertNotNull(protecao.getBloqueadaAte());
        assertEquals(5, eventoRepository.countByTipo(TipoEventoSeguranca.LOGIN_FALHA));
        assertEquals(1, eventoRepository.countByTipo(
                TipoEventoSeguranca.BLOQUEIO_TEMPORARIO_ATIVADO));
        assertEquals(1, eventoRepository.countByTipo(
                TipoEventoSeguranca.LOGIN_RECUSADO_BLOQUEIO));
    }

    @Test
    void sucessoDeveLimparFalhasAnteriores() throws Exception {
        criarConta("login.recuperado", PerfilAcesso.OPERADOR);
        loginEsperandoNaoAutorizado("login.recuperado", "senha-incorreta");
        autenticar("login.recuperado");
        assertEquals(0, protecaoRepository.findAll().getFirst().getFalhasConsecutivas());
        assertEquals(1, eventoRepository.countByTipo(TipoEventoSeguranca.LOGIN_SUCESSO));
    }

    @Test
    void respostaDeveSerGenericaParaLoginDesconhecido() throws Exception {
        loginEsperandoNaoAutorizado("conta.inexistente", "senha-incorreta");
        var evento = eventoRepository.findAll().getFirst();
        assertEquals(TipoEventoSeguranca.LOGIN_FALHA, evento.getTipo());
        assertNotNull(evento.getIdentificadorHash());
        assertFalse(evento.getIdentificadorHash().contains("conta.inexistente"));
    }

    @Test
    void somenteAdministradorDeveConsultarAuditoria() throws Exception {
        criarConta("auditor.admin", PerfilAcesso.ADMINISTRADOR);
        criarConta("auditor.operador", PerfilAcesso.OPERADOR);
        String tokenOperador = autenticar("auditor.operador");

        mockMvc.perform(get("/api/auditoria-seguranca")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperador))
                .andExpect(status().isForbidden());

        String tokenAdmin = autenticar("auditor.admin");
        mockMvc.perform(get("/api/auditoria-seguranca")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").isArray());

        mockMvc.perform(get("/api/auditoria-seguranca")
                        .param("tipo", "LOGIN_SUCESSO")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].tipo").value("LOGIN_SUCESSO"))
                .andExpect(jsonPath("$.conteudo[0].identificadorHash").doesNotExist());
    }

    private void loginEsperandoNaoAutorizado(String login, String senha) throws Exception {
        mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin(login, senha)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Login ou senha inválidos"));
    }

    private String autenticar(String login) throws Exception {
        String resposta = mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin(login, SENHA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.accessToken");
    }

    private String corpoLogin(String login, String senha) {
        return "{\"login\":\"%s\",\"senha\":\"%s\"}".formatted(login, senha);
    }

    private void criarConta(String login, PerfilAcesso perfil) {
        long semente = Math.abs(System.nanoTime());
        Pessoa pessoa = pessoaRepository.saveAndFlush(new Pessoa(
                "Pessoa Auditoria", "auditoria." + semente + "@example.com",
                gerarCpf(semente), LocalDate.of(2026, 10, 29)));
        ContaAcesso conta = new ContaAcesso(pessoa, login, passwordEncoder.encode(SENHA));
        conta.concederPerfil(perfil);
        contasCriadas.add(contaRepository.saveAndFlush(conta));
        pessoasCriadas.add(pessoa);
    }

    private String gerarCpf(long semente) {
        String base = "%09d".formatted(semente % 1_000_000_000L);
        if (base.chars().distinct().count() == 1) base = "123456789";
        int primeiro = digitoCpf(base, 10);
        return base + primeiro + digitoCpf(base + primeiro, 11);
    }

    private int digitoCpf(String numeros, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < numeros.length(); i++) {
            soma += Character.getNumericValue(numeros.charAt(i)) * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
