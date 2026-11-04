package com.curso.jogos.application;

public class RefreshTokenInvalidoException extends CredenciaisInvalidasException {
    public RefreshTokenInvalidoException() {
        super("Refresh token inválido ou expirado");
    }
}
