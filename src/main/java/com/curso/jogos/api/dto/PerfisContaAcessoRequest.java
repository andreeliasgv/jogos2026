package com.curso.jogos.api.dto;

import com.curso.jogos.domain.PerfilAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

@Schema(description = "Perfis que substituirão os perfis atuais da conta")
public record PerfisContaAcessoRequest(
        @NotEmpty(message = "Informe ao menos um perfil")
        @Schema(example = "[\"OPERADOR\", \"ADMINISTRADOR\"]")
        Set<PerfilAcesso> perfis) {
}
