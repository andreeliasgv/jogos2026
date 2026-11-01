package com.curso.jogos.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VendaTest {
    @Test
    void deveCalcularTotalComPrecoCapturadoNoMomentoDaInclusao() {
        Venda venda = novaVenda();
        Jogo jogo = novoJogo();
        venda.adicionarItem(jogo, 2);

        jogo.alterarPrecoUnitario(new BigDecimal("179.90"));

        assertEquals(new BigDecimal("299.80"), venda.calcularTotal());
        venda.finalizar();
        assertEquals(StatusVenda.FINALIZADA, venda.getStatus());
    }

    @Test
    void naoDeveAlterarVendaFinalizada() {
        Venda venda = novaVenda();
        venda.adicionarItem(novoJogo(), 1);
        venda.finalizar();

        assertThrows(IllegalStateException.class, venda::cancelar);
    }

    @Test
    void naoDeveFinalizarVendaVazia() {
        assertThrows(IllegalArgumentException.class, () -> novaVenda().finalizar());
    }

    private Venda novaVenda() {
        Pessoa pessoaCliente = new Pessoa("Cliente", "cliente@example.com", "52998224725", LocalDate.now());
        Pessoa pessoaVendedor = new Pessoa("Vendedor", "vendedor@example.com", "11144477735", LocalDate.now());
        return new Venda(new Cliente(pessoaCliente, null),
                new Colaborador(pessoaVendedor, "VEN-1", FuncaoColaborador.VENDEDOR, LocalDate.now()),
                LocalDateTime.of(2026, 10, 31, 10, 0));
    }

    private Jogo novoJogo() {
        return new Jogo("J-1", "Jogo", 10, new BigDecimal("149.90"), LocalDate.now());
    }
}
