package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.LoginRequest;
import com.curso.jogos.api.dto.RefreshTokenRequest;
import com.curso.jogos.api.dto.TokenResponse;
import com.curso.jogos.api.exception.ApiError;
import com.curso.jogos.application.AutenticacaoService;
import com.curso.jogos.application.TokensAutenticacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação", description = "Emissão didática de JWT para contas locais")
@RestController
@RequestMapping("/api/autenticacao")
public class AutenticacaoController {
    private final AutenticacaoService service;

    public AutenticacaoController(AutenticacaoService service) {
        this.service = service;
    }

    @Operation(
            summary = "Autenticar e emitir JWT",
            description = "Valida login e senha e devolve um Bearer Token válido por 30 minutos.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token emitido"),
            @ApiResponse(responseCode = "400", description = "JSON ou campos inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Login ou senha inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request,
                               HttpServletRequest httpRequest) {
        return toResponse(service.autenticar(
                request.login(), request.senha(), httpRequest.getRemoteAddr()));
    }

    @Operation(summary = "Renovar tokens com rotação do refresh token")
    @SecurityRequirements
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return toResponse(service.renovar(request.refreshToken()));
    }

    @Operation(summary = "Encerrar a sessão representada pelo refresh token")
    @SecurityRequirements
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        service.encerrar(request.refreshToken());
    }

    @Operation(summary = "Encerrar todas as sessões da conta autenticada")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout-todos")
    public void logoutTodos(Authentication authentication) {
        service.encerrarTodas(authentication.getName());
    }

    private TokenResponse toResponse(TokensAutenticacao tokens) {
        return new TokenResponse(
                tokens.acesso().valor(),
                "Bearer",
                tokens.acesso().duracaoSegundos(),
                tokens.acesso().expiraEm(),
                tokens.refreshToken(),
                tokens.refreshTokenExpiraEm());
    }
}
