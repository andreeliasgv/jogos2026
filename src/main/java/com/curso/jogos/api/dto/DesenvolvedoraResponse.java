package com.curso.jogos.api.dto;

import com.curso.jogos.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de uma desenvolvedora")
public record DesenvolvedoraResponse(
        @Schema(description = "Identificador da desenvolvedora", example = "1")
        Long id,
        @Schema(description = "Nome da desenvolvedora", example = "Estúdio Aurora")
        String nome,
        @Schema(description = "Código único da desenvolvedora", example = "ESTUDIO-AURORA")
        String codigo,
        @Schema(description = "Situação da desenvolvedora", example = "ATIVO")
        Status status) {
}
