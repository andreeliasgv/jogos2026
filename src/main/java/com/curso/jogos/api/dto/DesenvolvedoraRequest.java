package com.curso.jogos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DesenvolvedoraRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve possuir no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "Código é obrigatório")
        @Pattern(
                regexp = "[A-Za-z0-9-]{2,30}",
                message = "Código deve possuir entre 2 e 30 caracteres alfanuméricos ou hífen")
        String codigo) {
}
