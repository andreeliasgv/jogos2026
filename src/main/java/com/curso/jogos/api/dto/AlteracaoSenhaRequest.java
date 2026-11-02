package com.curso.jogos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlteracaoSenhaRequest(
        @NotBlank(message = "Senha atual é obrigatória")
        @Size(min = 12, max = 72, message = "Senha atual deve possuir entre 12 e 72 caracteres")
        @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
        String senhaAtual,

        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 12, max = 72, message = "Nova senha deve possuir entre 12 e 72 caracteres")
        @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
        String novaSenha) {
}
