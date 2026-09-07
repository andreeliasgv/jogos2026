package com.curso.jogos.api.dto;

import com.curso.jogos.domain.Status;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JogoResponse(
        Long id,
        String codigoJogo,
        String titulo,
        int quantidadeDisponivel,
        BigDecimal precoUnitario,
        int estoqueMinimo,
        BigDecimal valorInventario,
        LocalDate dataCadastro,
        Status status,
        Long generoId,
        String generoNome,
        Long desenvolvedoraId,
        String desenvolvedoraNome) {
}