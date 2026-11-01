package com.curso.jogos.api.dto;

import com.curso.jogos.domain.FuncaoColaborador;
import com.curso.jogos.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Representação do vínculo profissional e de sua pessoa")
public record ColaboradorResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "2") Long pessoaId,
        @Schema(example = "Bruno Lima") String nome,
        @Schema(example = "bruno.lima@example.com") String email,
        @Schema(example = "11144477735") String cpf,
        @Schema(example = "ATIVO") Status status,
        @Schema(example = "VEN-001") String matricula,
        @Schema(example = "VENDEDOR") FuncaoColaborador funcao,
        @Schema(example = "2026-10-31") LocalDate dataAdmissao) {
}
