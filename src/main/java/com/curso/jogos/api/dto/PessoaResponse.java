package com.curso.jogos.api.dto;

import com.curso.jogos.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Representação de uma pessoa")
public record PessoaResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Ana Souza") String nome,
        @Schema(example = "ana.souza@example.com") String email,
        @Schema(example = "52998224725") String cpf,
        @Schema(example = "2026-10-31") LocalDate dataCadastro,
        @Schema(example = "ATIVO") Status status) {
}
