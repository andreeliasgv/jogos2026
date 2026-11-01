package com.curso.jogos.api.dto;

import com.curso.jogos.domain.FuncaoColaborador;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para associar uma pessoa ao papel de colaborador")
public record ColaboradorRequest(
        @NotNull(message = "Pessoa é obrigatória") @Positive
        @Schema(example = "2") Long pessoaId,
        @NotBlank(message = "Matrícula é obrigatória") @Size(max = 30)
        @Schema(example = "VEN-001") String matricula,
        @NotNull(message = "Função é obrigatória")
        @Schema(example = "VENDEDOR") FuncaoColaborador funcao,
        @NotNull(message = "Data de admissão é obrigatória")
        @Schema(example = "2026-10-31") LocalDate dataAdmissao) {
}
