package com.curso.jogos.api.dto;

import com.curso.jogos.domain.Status;

public record DesenvolvedoraResponse(
        Long id,
        String nome,
        String codigo,
        Status status) {
}