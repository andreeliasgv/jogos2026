package com.curso.jogos.api.dto;

import com.curso.jogos.domain.StatusContaAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Novo status administrativo da conta")
public record StatusContaAcessoRequest(
        @NotNull(message = "Status é obrigatório")
        @Schema(example = "BLOQUEADA")
        StatusContaAcesso status) {
}
