package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.AlteracaoSenhaRequest;
import com.curso.jogos.api.dto.ContaAcessoRequest;
import com.curso.jogos.api.dto.ContaAcessoResponse;
import com.curso.jogos.api.exception.ApiError;
import com.curso.jogos.api.mapper.ContaAcessoMapper;
import com.curso.jogos.application.ContaAcessoService;
import com.curso.jogos.domain.ContaAcesso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Contas de acesso", description = "Credenciais, autenticação e perfis da API")
@RestController
@RequestMapping("/api/contas-acesso")
public class ContaAcessoController {
    private final ContaAcessoService service;
    private final ContaAcessoMapper mapper;

    public ContaAcessoController(ContaAcessoService service, ContaAcessoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(
            summary = "Criar conta de acesso",
            description = "Bootstrap público provisório. Cria conta ativa somente com o perfil OPERADOR.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada sem expor a senha"),
            @ApiResponse(responseCode = "400", description = "Dados ou senha inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Pessoa ou login já cadastrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<ContaAcessoResponse> cadastrar(
            @Valid @RequestBody ContaAcessoRequest request) {
        ContaAcesso conta = service.cadastrar(request.pessoaId(), request.login(), request.senha());
        return ResponseEntity.created(URI.create("/api/contas-acesso/me"))
                .body(mapper.toResponse(conta));
    }

    @Operation(summary = "Consultar a própria conta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta autenticada"),
            @ApiResponse(responseCode = "401", description = "Credencial ausente ou inválida")
    })
    @GetMapping("/me")
    public ContaAcessoResponse consultarPropriaConta(Authentication authentication) {
        return mapper.toResponse(service.buscarPorLogin(authentication.getName()));
    }

    @Operation(
            summary = "Alterar a própria senha",
            description = "Confere a senha atual e grava somente o novo hash BCrypt.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", description = "Senha atual inválida ou nova senha repetida",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Credencial ausente ou inválida")
    })
    @PutMapping("/me/senha")
    public ResponseEntity<Void> alterarSenha(
            Authentication authentication,
            @Valid @RequestBody AlteracaoSenhaRequest request) {
        service.alterarSenha(authentication.getName(), request.senhaAtual(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }
}
