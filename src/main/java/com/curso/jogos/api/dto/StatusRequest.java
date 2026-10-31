package com.curso.jogos.api.dto;

import com.curso.jogos.domain.Status;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(
        @NotNull(message = "Status é obrigatório")
        Status status) {
}
