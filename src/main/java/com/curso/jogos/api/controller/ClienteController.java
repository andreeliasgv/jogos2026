package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.ClienteAtualizacaoRequest;
import com.curso.jogos.api.dto.ClienteRequest;
import com.curso.jogos.api.dto.ClienteResponse;
import com.curso.jogos.api.dto.PaginaResponse;
import com.curso.jogos.api.mapper.ClienteMapper;
import com.curso.jogos.application.ClienteService;
import com.curso.jogos.domain.Cliente;
import com.curso.jogos.domain.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Clientes", description = "Papel comercial associado a uma pessoa")
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;
    private final ClienteMapper mapper;

    public ClienteController(ClienteService service, ClienteMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar cliente", description = "Associa o papel a uma pessoa ativa.")
    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest request) {
        Cliente cliente = service.cadastrar(request.pessoaId(), request.telefone());
        return ResponseEntity.created(URI.create("/api/clientes/" + cliente.getId()))
                .body(mapper.toResponse(cliente));
    }

    @Operation(summary = "Consultar cliente por ID")
    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(summary = "Pesquisar clientes")
    @GetMapping
    public PaginaResponse<ClienteResponse> pesquisar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) Status status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return PaginaResponse.de(service.pesquisar(nome, cpf, status, pageable), mapper::toResponse);
    }

    @Operation(summary = "Alterar cliente", description = "Altera somente os dados próprios do papel.")
    @PutMapping("/{id}")
    public ClienteResponse alterar(@PathVariable Long id,
                                   @Valid @RequestBody ClienteAtualizacaoRequest request) {
        return mapper.toResponse(service.alterar(id, request.telefone()));
    }
}
