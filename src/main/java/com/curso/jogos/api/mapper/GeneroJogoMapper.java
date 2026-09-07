package com.curso.jogos.api.mapper;

import com.curso.jogos.api.dto.GeneroJogoResponse;
import com.curso.jogos.domain.GeneroJogo;
import org.springframework.stereotype.Component;

@Component
public class GeneroJogoMapper {

    public GeneroJogoResponse toResponse(GeneroJogo genero) {
        return new GeneroJogoResponse(
                genero.getId(),
                genero.getNome(),
                genero.getStatus());
    }
}