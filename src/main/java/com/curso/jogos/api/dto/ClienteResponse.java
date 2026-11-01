package com.curso.jogos.api.dto;

import com.curso.jogos.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação do papel de cliente e de sua pessoa")
public record ClienteResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long pessoaId,
        @Schema(example = "Ana Souza") String nome,
        @Schema(example = "ana.souza@example.com") String email,
        @Schema(example = "52998224725") String cpf,
        @Schema(example = "(11) 99999-0000") String telefone,
        @Schema(example = "ATIVO") Status status) {
}
