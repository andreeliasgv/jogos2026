package com.curso.jogos.application;

import com.curso.jogos.domain.ContaAcesso;
import com.curso.jogos.domain.Pessoa;
import com.curso.jogos.repository.ContaAcessoRepository;
import com.curso.jogos.repository.PessoaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(roles = "ADMINISTRADOR")
class ContaAcessoServiceTest {
    private static final String SENHA = "uma-frase-senha-2026";

    @Autowired ContaAcessoService service;
    @Autowired ContaAcessoRepository contaRepository;
    @Autowired PessoaRepository pessoaRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbcTemplate;

    @AfterEach
    void limparDados() {
        jdbcTemplate.update("DELETE FROM conta_acesso_perfil");
        jdbcTemplate.update("DELETE FROM conta_acesso");
        jdbcTemplate.update("DELETE FROM pessoa WHERE email LIKE '%@conta.test'");
    }

    @Test
    void deveCadastrarContaArmazenandoSomenteHashBcrypt() {
        Pessoa pessoa = criarPessoa("hash");

        service.cadastrar(pessoa.getId(), "  Operadora.2026  ", SENHA, "admin.teste");

        ContaAcesso persistida = contaRepository.findByLoginIgnoreCase("operadora.2026")
                .orElseThrow();
        assertNotEquals(SENHA, persistida.getSenhaHash());
        assertTrue(persistida.getSenhaHash().startsWith("{bcrypt}"));
        assertTrue(passwordEncoder.matches(SENHA, persistida.getSenhaHash()));
        assertTrue(persistida.podeAutenticar());
    }

    @Test
    void deveAlterarSenhaInvalidandoACredencialAnterior() {
        Pessoa pessoa = criarPessoa("alteracao");
        service.cadastrar(pessoa.getId(), "alteracao.2026", SENHA, "admin.teste");

        String novaSenha = "outra-frase-senha-2026";
        service.alterarSenha("alteracao.2026", SENHA, novaSenha);

        ContaAcesso persistida = contaRepository.findByLoginIgnoreCase("alteracao.2026")
                .orElseThrow();
        assertFalse(passwordEncoder.matches(SENHA, persistida.getSenhaHash()));
        assertTrue(passwordEncoder.matches(novaSenha, persistida.getSenhaHash()));
    }

    private Pessoa criarPessoa(String prefixo) {
        long semente = System.nanoTime();
        return pessoaRepository.save(new Pessoa(
                "Pessoa " + prefixo,
                prefixo + semente + "@conta.test",
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
