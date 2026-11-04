package com.curso.jogos.api;

import com.curso.jogos.domain.ContaAcesso;
import com.curso.jogos.domain.Pessoa;
import com.curso.jogos.repository.ContaAcessoRepository;
import com.curso.jogos.repository.PessoaRepository;
import com.curso.jogos.repository.SessaoRefreshTokenRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CicloTokenApiTest {
    private static final String SENHA = "senha-ciclo-token-2026";

    @Autowired MockMvc mockMvc;
    @Autowired PessoaRepository pessoaRepository;
    @Autowired ContaAcessoRepository contaRepository;
    @Autowired SessaoRefreshTokenRepository sessaoRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtDecoder jwtDecoder;

    private ContaAcesso contaCriada;
    private Pessoa pessoaCriada;

    @AfterEach
    void limparCenario() {
        sessaoRepository.deleteAll();
        if (contaCriada != null) contaRepository.deleteById(contaCriada.getId());
        if (pessoaCriada != null) pessoaRepository.deleteById(pessoaCriada.getId());
    }

    @Test
    void deveRotacionarRefreshTokenERevogarAFamiliaQuandoHouverReutilizacao()
            throws Exception {
        criarConta("rotacao.token");
        TokensHttp primeiro = autenticar("rotacao.token");
        Jwt jwt = jwtDecoder.decode(primeiro.accessToken());
        assertNotNull(jwt.getClaimAsString("sid"));

        TokensHttp segundo = renovar(primeiro.refreshToken());
        assertNotEquals(primeiro.refreshToken(), segundo.refreshToken());
        assertNotEquals(primeiro.accessToken(), segundo.accessToken());
        renovarEsperandoNaoAutorizado(primeiro.refreshToken());
        renovarEsperandoNaoAutorizado(segundo.refreshToken());
    }

    @Test
    void deveImpedirRenovacaoAposLogoutSemInvalidarOAccessTokenJaEmitido()
            throws Exception {
        criarConta("logout.token");
        TokensHttp tokens = autenticar("logout.token");
        mockMvc.perform(post("/api/autenticacao/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRefresh(tokens.refreshToken())))
                .andExpect(status().isNoContent());
        renovarEsperandoNaoAutorizado(tokens.refreshToken());
        mockMvc.perform(get("/api/contas-acesso/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("logout.token"));
    }

    @Test
    void deveEncerrarTodasAsSessoesDaContaAutenticada() throws Exception {
        criarConta("logout.global");
        TokensHttp primeira = autenticar("logout.global");
        TokensHttp segunda = autenticar("logout.global");
        mockMvc.perform(post("/api/autenticacao/logout-todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + primeira.accessToken()))
                .andExpect(status().isNoContent());
        renovarEsperandoNaoAutorizado(primeira.refreshToken());
        renovarEsperandoNaoAutorizado(segunda.refreshToken());
    }

    private TokensHttp autenticar(String login) throws Exception {
        String resposta = mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"%s\",\"senha\":\"%s\"}"
                                .formatted(login, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshTokenExpiresAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return new TokensHttp(JsonPath.read(resposta, "$.accessToken"),
                JsonPath.read(resposta, "$.refreshToken"));
    }

    private TokensHttp renovar(String refreshToken) throws Exception {
        String resposta = mockMvc.perform(post("/api/autenticacao/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRefresh(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return new TokensHttp(JsonPath.read(resposta, "$.accessToken"),
                JsonPath.read(resposta, "$.refreshToken"));
    }

    private void renovarEsperandoNaoAutorizado(String refreshToken) throws Exception {
        mockMvc.perform(post("/api/autenticacao/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRefresh(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token inválido ou expirado"));
    }

    private String corpoRefresh(String refreshToken) {
        return "{\"refreshToken\":\"%s\"}".formatted(refreshToken);
    }

    private void criarConta(String login) {
        long semente = Math.abs(System.nanoTime());
        pessoaCriada = pessoaRepository.saveAndFlush(new Pessoa("Pessoa Ciclo Token",
                "ciclo." + semente + "@example.com", gerarCpf(semente),
                LocalDate.of(2026, 10, 29)));
        contaCriada = contaRepository.saveAndFlush(new ContaAcesso(
                pessoaCriada, login, passwordEncoder.encode(SENHA)));
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

    private record TokensHttp(String accessToken, String refreshToken) {}
}
