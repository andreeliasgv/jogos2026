package com.curso.jogos.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GeneroJogoTest {
    @Test
    void deveAdicionarJogoEManterOsDoisLadosDaAssociacao() {
        GeneroJogo genero = new GeneroJogo("Aventura");
        Jogo jogo = novoJogo("JG-001");

        genero.adicionarJogo(jogo);

        assertEquals(1, genero.getJogos().size());
        assertSame(jogo, genero.getJogos().getFirst());
        assertSame(genero, jogo.getGenero());
    }

    @Test
    void naoDeveAdicionarJogoNulo() {
        GeneroJogo genero = new GeneroJogo("Aventura");

        assertThrows(NullPointerException.class,
                () -> genero.adicionarJogo(null));
    }

    @Test
    void naoDeveAdicionarDoisJogosComOMesmoCodigo() {
        GeneroJogo genero = new GeneroJogo("Aventura");

        genero.adicionarJogo(novoJogo("JG-001"));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> genero.adicionarJogo(novoJogo("JG-001")));

        assertEquals(
                "Código do jogo já utilizado no gênero",
                excecao.getMessage());
    }

    @Test
    void naoDevePermitirQueJogoPertencaADoisGeneros() {
        GeneroJogo aventura = new GeneroJogo("Aventura");
        GeneroJogo estrategia = new GeneroJogo("Estratégia");
        Jogo jogo = novoJogo("JG-001");

        aventura.adicionarJogo(jogo);

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> estrategia.adicionarJogo(jogo));

        assertEquals(
                "Jogo já pertence a outro gênero",
                excecao.getMessage());
    }

    @Test
    void naoDeveExporUmaListaInternaModificavel() {
        GeneroJogo genero = new GeneroJogo("Aventura");
        genero.adicionarJogo(novoJogo("JG-001"));

        assertThrows(
                UnsupportedOperationException.class,
                () -> genero.getJogos().add(novoJogo("JG-002")));
    }

    private Jogo novoJogo(String codigo) {
        return new Jogo(
                codigo,
                "Ruinas de Aurion",
                3,
                new BigDecimal("79.90"),
                LocalDate.of(2026,8,20));
    }
}
