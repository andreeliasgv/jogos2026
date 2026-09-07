package com.curso.jogos.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DesenvolvedoraTest {

    @Test
    void deveCriarDesenvolvedoraAtiva() {
        Desenvolvedora desenvolvedora = new Desenvolvedora(
                " Larian Studios ",
                "larian");

        assertEquals("Larian Studios", desenvolvedora.getNome());
        assertEquals("LARIAN", desenvolvedora.getCodigo());
        assertEquals(Status.ATIVO, desenvolvedora.getStatus());
    }

    @Test
    void deveRejeitarCodigoComFormatoInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Desenvolvedora(
                        "Desenvolvedora",
                        "código inválido"));
    }

    @Test
    void deveRejeitarNomeVazio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Desenvolvedora(
                        " ",
                        "TESTE"));
    }

    @Test
    void deveInativarDesenvolvedora() {
        Desenvolvedora desenvolvedora = new Desenvolvedora(
                "Desenvolvedora",
                "TESTE");

        desenvolvedora.inativar();

        assertEquals(Status.INATIVO, desenvolvedora.getStatus());
    }
}