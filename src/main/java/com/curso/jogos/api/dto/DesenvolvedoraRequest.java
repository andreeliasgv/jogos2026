package com.curso.jogos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar uma desenvolvedora")
public record DesenvolvedoraRequest(
        @Schema(description = "Nome da desenvolvedora", example = "Estúdio Aurora")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve possuir no máximo 150 caracteres")
        String nome,

        @Schema(description = "Código único com letras, números ou hífen",
                example = "ESTUDIO-AURORA")
        @NotBlank(message = "Código é obrigatório")
        @Pattern(
                regexp = "[A-Za-z0-9-]{2,30}",
                message = "Código deve possuir entre 2 e 30 caracteres alfanuméricos ou hífen")
        String codigo) {
}
