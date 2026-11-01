package com.curso.jogos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Jogo e quantidade incluídos em uma venda aberta")
public record ItemVendaRequest(
        @NotNull(message = "Jogo é obrigatório") @Positive
        @Schema(example = "1") Long jogoId,
        @NotNull(message = "Quantidade é obrigatória") @Positive
        @Schema(example = "2") Integer quantidade) {
}
