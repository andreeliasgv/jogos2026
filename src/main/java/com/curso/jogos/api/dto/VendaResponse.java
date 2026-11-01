package com.curso.jogos.api.dto;

import com.curso.jogos.domain.StatusVenda;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Venda completa com participantes, itens e total histórico")
public record VendaResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long clienteId,
        @Schema(example = "Ana Souza") String clienteNome,
        @Schema(example = "1") Long vendedorId,
        @Schema(example = "Bruno Lima") String vendedorNome,
        LocalDateTime dataVenda,
        @Schema(example = "ABERTA") StatusVenda status,
        List<ItemVendaResponse> itens,
        @Schema(example = "299.80") BigDecimal total) {
}
