package com.curso.jogos.api.dto;

import com.curso.jogos.domain.FuncaoColaborador;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados editáveis do vínculo profissional")
public record ColaboradorAtualizacaoRequest(
        @NotBlank(message = "Matrícula é obrigatória") @Size(max = 30)
        @Schema(example = "VEN-001") String matricula,
        @NotNull(message = "Função é obrigatória")
        @Schema(example = "GERENTE") FuncaoColaborador funcao) {
}
