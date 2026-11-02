package com.curso.jogos.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContaAcessoTest {

    @Test
    void deveCriarContaAtivaComLoginNormalizadoEPerfilOperador() {
        ContaAcesso conta = new ContaAcesso(
                novaPessoa(),
                "  ALUNA.2026  ",
                "  {bcrypt}hash-seguro  ");

        assertEquals("aluna.2026", conta.getLogin());
        assertEquals("{bcrypt}hash-seguro", conta.getSenhaHash());
        assertEquals(StatusContaAcesso.ATIVA, conta.getStatus());
        assertEquals(Set.of(PerfilAcesso.OPERADOR), conta.getPerfis());
        assertTrue(conta.podeAutenticar());
    }

    @Test
    void deveAdministrarPerfisSemPermitirContaSemPerfil() {
        ContaAcesso conta = novaConta();

        conta.concederPerfil(PerfilAcesso.ADMINISTRADOR);
        conta.substituirPerfis(Set.of(PerfilAcesso.ADMINISTRADOR));

        assertTrue(conta.possuiPerfil(PerfilAcesso.ADMINISTRADOR));
        assertFalse(conta.possuiPerfil(PerfilAcesso.OPERADOR));
        assertThrows(IllegalArgumentException.class,
                () -> conta.substituirPerfis(Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> conta.substituirPerfis(null));
    }

    @Test
    void deveConsiderarStatusDaContaEDaPessoaAoAutenticar() {
        Pessoa pessoa = novaPessoa();
        ContaAcesso conta = new ContaAcesso(pessoa, "aluna.2026", "{bcrypt}hash");

        conta.bloquear();
        assertFalse(conta.podeAutenticar());

        conta.ativar();
        pessoa.inativar();
        assertFalse(conta.podeAutenticar());

        pessoa.ativar();
        assertTrue(conta.podeAutenticar());
    }

    private ContaAcesso novaConta() {
        return new ContaAcesso(novaPessoa(), "aluna.2026", "{bcrypt}hash");
    }

    private Pessoa novaPessoa() {
        return new Pessoa(
                "Aluna de Teste",
                "aluna.teste@example.com",
                "52998224725",
                LocalDate.of(2026, 11, 2));
    }
}
