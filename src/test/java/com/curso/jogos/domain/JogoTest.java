package com.curso.jogos.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JogoTest {
    @Test
    void deveCriarJogoAtivoComDadosValidos() {
        Jogo jogo = novoJogo(3, "79.90");

        assertEquals("JG-001", jogo.getCodigoJogo());
        assertEquals("Ruinas de Aurion", jogo.getTitulo());
        assertEquals(3, jogo.getQuantidadeDisponivel());
        assertEquals(Status.ATIVO, jogo.getStatus());
        assertEquals(LocalDate.of(2026, 8, 20), jogo.getDataCadastro());
    }

    @Test
    void deveCalcularValorDoInventario() {
        Jogo jogo = novoJogo(3, "79.90");

        BigDecimal valor = jogo.calcularValorInventario();

        assertEquals(0, new BigDecimal("239.70").compareTo(valor));
    }

    @Test
    void deveReceberERetirarUnidades() {
        Jogo jogo = novoJogo(3, "79.90");

        jogo.receberUnidades(2);
        jogo.retirarUnidades(1);

        assertEquals(4, jogo.getQuantidadeDisponivel());
    }

    @Test
    void naoDeveRetirarQuantidadeMaiorQueADisponivel() {
        Jogo jogo = novoJogo(3, "79.90");

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class, () -> jogo.retirarUnidades(4));

        assertEquals("Quantidade disponível insuficiente", excecao.getMessage());
    }

    @Test
    void naoDeveCriarJogoComCodigoEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Jogo("  ",
                        "Ruinas de Aurion",
                3,
                new BigDecimal("79.90"),
                        LocalDate.of(2026, 8, 20)));
    }

    @Test
    void naoDeveCriarJogoComQuantidadeNegativa() {
        assertThrows(
                IllegalArgumentException.class,
                () -> novoJogo(-1, "79.90"));
    }

    @Test
    void deveAlterarOStatusPorComportamentoExplicito() {
        Jogo jogo = novoJogo(3, "79.90");

        jogo.inativar();
        assertEquals(Status.INATIVO, jogo.getStatus());

        jogo.ativar();
        assertEquals(Status.ATIVO, jogo.getStatus());
    }

    @Test
    void deveCriarJogoComEstoqueMinimoEAssociarDesenvolvedora() {
        Jogo jogo = new Jogo(
                "JG-002",
                "Divinity: Original Sin 2",
                5,
                new BigDecimal("89.90"),
                2,
                LocalDate.of(2026, 9, 3));

        Desenvolvedora desenvolvedora = new Desenvolvedora(
                "Larian Studios",
                "LARIAN");

        jogo.associarDesenvolvedora(desenvolvedora);

        assertEquals(2, jogo.getEstoqueMinimo());
        assertEquals(desenvolvedora, jogo.getDesenvolvedora());
    }

    @Test
    void naoDeveCriarJogoComEstoqueMinimoNegativo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Jogo(
                        "JG-003",
                        "Jogo inválido",
                        5,
                        new BigDecimal("79.90"),
                        -1,
                        LocalDate.of(2026, 9, 3)));
    }

    private Jogo novoJogo(int quantidade, String precoUnitario) {
        return new Jogo("JG-001",
                "Ruinas de Aurion",
                quantidade,
                new BigDecimal(precoUnitario),
                LocalDate.of(2026, 8, 20));
    }
}