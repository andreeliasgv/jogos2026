package com.curso.jogos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Bearer Token emitido após autenticação")
public record TokenResponse(
        @Schema(description = "JWT assinado", accessMode = Schema.AccessMode.READ_ONLY)
        String accessToken,
        @Schema(example = "Bearer")
        String tokenType,
        @Schema(example = "1800")
        long expiresIn,
        Instant expiresAt,
        @Schema(description = "Token opaco de uso único para renovação")
        String refreshToken,
        Instant refreshTokenExpiresAt) {
}
