package com.curso.jogos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContaAcessoRequest(
        @NotNull(message = "Pessoa é obrigatória")
        @Schema(example = "1")
        Long pessoaId,

        @NotBlank(message = "Login é obrigatório")
        @Size(min = 3, max = 80, message = "Login deve possuir entre 3 e 80 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$",
                message = "Login aceita apenas letras, números, ponto, sublinhado e hífen")
        @Schema(example = "operadora.2026")
        String login,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 12, max = 72, message = "Senha deve possuir entre 12 e 72 caracteres")
        @Schema(example = "uma-frase-senha-2026", accessMode = Schema.AccessMode.WRITE_ONLY)
        String senha) {
}
