package com.curso.jogos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Login é obrigatório")
        @Size(max = 80, message = "Login deve possuir no máximo 80 caracteres")
        @Schema(example = "operadora.2026")
        String login,

        @NotBlank(message = "Senha é obrigatória")
        @Size(max = 72, message = "Senha deve possuir no máximo 72 caracteres")
        @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
        String senha) {
}
