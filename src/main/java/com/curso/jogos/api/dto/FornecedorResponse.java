package com.curso.jogos.api.dto;

import com.curso.jogos.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um fornecedor")
public record FornecedorResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Distribuidora Aurora Ltda.") String razaoSocial,
        @Schema(example = "12345678000195") String cnpj,
        @Schema(example = "ATIVO") Status status) {
}
