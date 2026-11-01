package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.FornecedorRequest;
import com.curso.jogos.api.dto.FornecedorResponse;
import com.curso.jogos.api.exception.ApiError;
import com.curso.jogos.api.mapper.FornecedorMapper;
import com.curso.jogos.application.FornecedorService;
import com.curso.jogos.domain.Fornecedor;
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

@Tag(name = "Fornecedores", description = "Cadastro e consulta de fornecedores de jogos")
@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {
    private final FornecedorService service;
    private final FornecedorMapper mapper;

    public FornecedorController(FornecedorService service, FornecedorMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar fornecedor")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fornecedor cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "CNPJ já utilizado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<FornecedorResponse> cadastrar(@Valid @RequestBody FornecedorRequest request) {
        Fornecedor fornecedor = service.cadastrar(mapper.toEntity(request));
        return ResponseEntity.created(URI.create("/api/fornecedores/" + fornecedor.getId()))
                .body(mapper.toResponse(fornecedor));
    }

    @Operation(summary = "Consultar fornecedor por ID")
    @GetMapping("/{id}")
    public FornecedorResponse buscar(@Parameter(example = "1") @PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(summary = "Listar fornecedores")
    @GetMapping
    public List<FornecedorResponse> listar() {
        return service.listar().stream().map(mapper::toResponse).toList();
    }
}
