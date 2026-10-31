package com.curso.jogos.api.dto;

import com.curso.jogos.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um gênero de jogo")
public record GeneroJogoResponse(
        @Schema(description = "Identificador do gênero", example = "1")
        Long id,
        @Schema(description = "Nome do gênero", example = "RPG")
        String nome,
        @Schema(description = "Situação do gênero", example = "ATIVO")
        Status status) {
}
