package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.DesenvolvedoraRequest;
import com.curso.jogos.api.dto.DesenvolvedoraResponse;
import com.curso.jogos.api.exception.ApiError;
import com.curso.jogos.api.mapper.DesenvolvedoraMapper;
import com.curso.jogos.application.DesenvolvedoraService;
import com.curso.jogos.domain.Desenvolvedora;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Tag(
        name = "Desenvolvedoras",
        description = "Cadastro e consulta de desenvolvedoras de jogos")
@RestController
@RequestMapping("/api/desenvolvedoras")
public class DesenvolvedoraController {

    private final DesenvolvedoraService service;
    private final DesenvolvedoraMapper mapper;

    public DesenvolvedoraController(
            DesenvolvedoraService service,
            DesenvolvedoraMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(
            summary = "Cadastrar desenvolvedora",
            description = "Cria uma desenvolvedora ativa e devolve sua localização.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Desenvolvedora cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Código já utilizado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<DesenvolvedoraResponse> cadastrar(
            @Valid @RequestBody DesenvolvedoraRequest request) {
        Desenvolvedora desenvolvedora =
                service.cadastrar(mapper.toEntity(request));

        URI location = URI.create(
                "/api/desenvolvedoras/" + desenvolvedora.getId());

        return ResponseEntity
                .created(location)
                .body(mapper.toResponse(desenvolvedora));
    }

    @Operation(
            summary = "Consultar desenvolvedora por ID",
            description = "Retorna os dados da desenvolvedora informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Desenvolvedora encontrada"),
            @ApiResponse(responseCode = "404", description = "Desenvolvedora não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public DesenvolvedoraResponse buscarPorId(
            @Parameter(description = "Identificador da desenvolvedora", example = "1")
            @PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(
            summary = "Listar desenvolvedoras",
            description = "Retorna todas as desenvolvedoras; este recurso ainda não possui paginação.")
    @ApiResponse(responseCode = "200", description = "Desenvolvedoras listadas")
    @GetMapping
    public List<DesenvolvedoraResponse> listar() {
        return service.listar()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
