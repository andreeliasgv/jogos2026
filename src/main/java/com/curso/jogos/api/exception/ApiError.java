package com.curso.jogos.api.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Resposta padronizada produzida quando uma requisição falha")
public record ApiError(
        @Schema(description = "Instante em que o erro ocorreu", example = "2026-10-31T21:30:00Z")
        Instant timestamp,
        @Schema(description = "Código HTTP numérico", example = "400")
        int status,
        @Schema(description = "Nome do erro HTTP", example = "Bad Request")
        String error,
        @Schema(description = "Explicação legível da falha", example = "Dados inválidos")
        String message,
        @Schema(description = "Caminho da requisição que falhou", example = "/api/jogos")
        String path,
        @Schema(description = "Erros de validação organizados pelo nome do campo")
        Map<String, String> fields) {
}
