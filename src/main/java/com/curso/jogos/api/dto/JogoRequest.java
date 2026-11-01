package com.curso.jogos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Dados para cadastrar um jogo")
public record JogoRequest(
        @Schema(description = "Código único do jogo", example = "JOGO-AVENTURA-001")
        @NotBlank(message = "Código do jogo é obrigatório")
        @Size(max = 50, message = "Código deve possuir no máximo 50 caracteres")
        String codigoJogo,

        @Schema(description = "Título comercial do jogo", example = "Aventura nas Estrelas")
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 150, message = "Título deve possuir no máximo 150 caracteres")
        String titulo,

        @Schema(description = "Quantidade inicial disponível", example = "10")
        @NotNull(message = "Quantidade disponível é obrigatória")
        @PositiveOrZero(message = "Quantidade disponível não pode ser negativa")
        Integer quantidadeDisponivel,

        @Schema(description = "Preço atual por unidade", example = "149.90")
        @NotNull(message = "Preço unitário é obrigatório")
        @PositiveOrZero(message = "Preço unitário não pode ser negativo")
        BigDecimal precoUnitario,

        @Schema(description = "Limite usado para indicar reposição", example = "2")
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
