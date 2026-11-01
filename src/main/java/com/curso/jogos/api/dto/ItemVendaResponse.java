package com.curso.jogos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Item de venda com preço histórico")
public record ItemVendaResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long jogoId,
        @Schema(example = "Aventura nas Estrelas") String jogoTitulo,
        @Schema(example = "2") int quantidade,
        @Schema(example = "149.90") BigDecimal precoUnitario,
        @Schema(example = "299.80") BigDecimal subtotal) {
}
