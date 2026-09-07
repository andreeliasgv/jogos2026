package com.curso.jogos.api.dto;

import com.curso.jogos.domain.Status;

public record GeneroJogoResponse(
        Long id,
        String nome,
        Status status) {
}
