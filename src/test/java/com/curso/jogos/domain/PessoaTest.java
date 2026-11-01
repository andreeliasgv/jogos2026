package com.curso.jogos.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PessoaTest {
    @Test
    void deveNormalizarEmailEPreservarCpfValido() {
        Pessoa pessoa = new Pessoa(" Ana Souza ", " ANA@EXAMPLE.COM ", "52998224725",
                LocalDate.of(2026, 10, 31));

        assertEquals("Ana Souza", pessoa.getNome());
        assertEquals("ana@example.com", pessoa.getEmail());
        assertEquals("52998224725", pessoa.getCpf());
        assertEquals(Status.ATIVO, pessoa.getStatus());
    }

    @Test
    void naoDeveAceitarCpfComDigitoVerificadorIncorreto() {
        assertThrows(IllegalArgumentException.class,
                () -> new Pessoa("Ana", "ana@example.com", "52998224724", LocalDate.now()));
    }
}
