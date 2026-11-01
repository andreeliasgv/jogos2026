package com.curso.jogos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Participantes necessários para abrir uma venda")
public record VendaRequest(
        @NotNull(message = "Cliente é obrigatório") @Positive
        @Schema(example = "1") Long clienteId,
        @NotNull(message = "Vendedor é obrigatório") @Positive
        @Schema(example = "1") Long vendedorId) {
}
