package com.curso.jogos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Campos substituíveis de um jogo; código, quantidade e status possuem fluxos próprios")
public record JogoAtualizacaoRequest(
        @Schema(description = "Título comercial do jogo", example = "Aventura nas Estrelas — Edição Especial")
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 150, message = "Título deve possuir no máximo 150 caracteres")
        String titulo,

        @Schema(description = "Preço atual por unidade", example = "179.90")
        @NotNull(message = "Preço unitário é obrigatório")
        @PositiveOrZero(message = "Preço unitário não pode ser negativo")
        BigDecimal precoUnitario,

        @Schema(description = "Limite usado para indicar reposição", example = "4")
        @NotNull(message = "Estoque mínimo é obrigatório")
        @PositiveOrZero(message = "Estoque mínimo não pode ser negativo")
        Integer estoqueMinimo,

        @Schema(description = "Identificador de um gênero existente", example = "1")
        @NotNull(message = "Gênero é obrigatório")
        @Positive(message = "Identificador do gênero deve ser positivo")
        Long generoId,

        @Schema(description = "Identificador opcional de uma desenvolvedora existente",
                example = "1", nullable = true)
        @Positive(message = "Identificador da desenvolvedora deve ser positivo")
        Long desenvolvedoraId,

        @Schema(description = "Identificador opcional do fornecedor",
                example = "1", nullable = true)
        @Positive(message = "Identificador do fornecedor deve ser positivo")
        Long fornecedorId) {
}
