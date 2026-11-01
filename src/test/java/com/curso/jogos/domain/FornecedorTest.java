package com.curso.jogos.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FornecedorTest {
    @Test
    void deveCadastrarFornecedorComCnpjValido() {
        Fornecedor fornecedor = new Fornecedor(" Distribuidora Aurora ", "12345678000195");

        assertEquals("Distribuidora Aurora", fornecedor.getRazaoSocial());
        assertEquals("12345678000195", fornecedor.getCnpj());
        assertEquals(Status.ATIVO, fornecedor.getStatus());
    }

    @Test
    void naoDeveAceitarCnpjComDigitoIncorreto() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fornecedor("Distribuidora", "12345678000194"));
    }
}
