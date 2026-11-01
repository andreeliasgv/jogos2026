package com.curso.jogos.api.dto;

import com.curso.jogos.domain.StatusVenda;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resumo de venda usado em pesquisas paginadas")
public record VendaResumoResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long clienteId,
        @Schema(example = "Ana Souza") String clienteNome,
        @Schema(example = "1") Long vendedorId,
        @Schema(example = "Bruno Lima") String vendedorNome,
        LocalDateTime dataVenda,
        @Schema(example = "FINALIZADA") StatusVenda status) {
}
