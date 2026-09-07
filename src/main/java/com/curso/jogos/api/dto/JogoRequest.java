package com.curso.jogos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record JogoRequest(
        @NotBlank(message = "Código do jogo é obrigatório")
        @Size(max = 50, message = "Código deve possuir no máximo 50 caracteres")
        String codigoJogo,

        @NotBlank(message = "Título é obrigatório")
        @Size(max = 150, message = "Título deve possuir no máximo 150 caracteres")
        String titulo,

        @NotNull(message = "Quantidade disponível é obrigatória")
        @PositiveOrZero(message = "Quantidade disponível não pode ser negativa")
        Integer quantidadeDisponivel,

        @NotNull(message = "Preço unitário é obrigatório")
        @PositiveOrZero(message = "Preço unitário não pode ser negativo")
        BigDecimal precoUnitario,

        @NotNull(message = "Estoque mínimo é obrigatório")
        @PositiveOrZero(message = "Estoque mínimo não pode ser negativo")
        Integer estoqueMinimo,

        @NotNull(message = "Gênero é obrigatório")
        @Positive(message = "Identificador do gênero deve ser positivo")
        Long generoId,

        @Positive(message = "Identificador da desenvolvedora deve ser positivo")
        Long desenvolvedoraId) {
}