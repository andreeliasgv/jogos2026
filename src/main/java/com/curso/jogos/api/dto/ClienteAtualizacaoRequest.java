package com.curso.jogos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados editáveis do papel de cliente")
public record ClienteAtualizacaoRequest(
        @Size(max = 30, message = "Telefone deve possuir no máximo 30 caracteres")
        @Schema(example = "(11) 98888-0000") String telefone) {
}
