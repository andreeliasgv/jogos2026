package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.GeneroJogoAtualizacaoRequest;
import com.curso.jogos.api.dto.GeneroJogoRequest;
import com.curso.jogos.api.dto.StatusRequest;
import com.curso.jogos.api.dto.GeneroJogoResponse;
import com.curso.jogos.api.dto.PaginaResponse;
import com.curso.jogos.api.mapper.GeneroJogoMapper;
import com.curso.jogos.application.GeneroJogoService;
import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.domain.Status;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.net.URI;

@RestController
@RequestMapping("/api/generos")
public class GeneroJogoController {

    private final GeneroJogoService service;
    private final GeneroJogoMapper mapper;

    public GeneroJogoController(
            GeneroJogoService service,
            GeneroJogoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<GeneroJogoResponse> cadastrar(
            @Valid @RequestBody GeneroJogoRequest request) {
        GeneroJogo genero = service.cadastrar(request.nome());
        URI location = URI.create("/api/generos/" + genero.getId());

        return ResponseEntity
                .created(location)
                .body(mapper.toResponse(genero));
    }

    @GetMapping("/{id}")
    public GeneroJogoResponse buscarPorId(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @GetMapping
    public PaginaResponse<GeneroJogoResponse> pesquisar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Status status,
            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.ASC)
            Pageable pageable) {

        return PaginaResponse.de(
                service.pesquisar(nome, status, pageable),
                mapper::toResponse
        );
    }

    @PutMapping("/{id}")
    public GeneroJogoResponse alterar(
            @PathVariable Long id,
            @Valid @RequestBody
            GeneroJogoAtualizacaoRequest request) {

        return mapper.toResponse(
                service.alterar(id, request.nome())
        );
    }

    @PutMapping("/{id}/status")
    public GeneroJogoResponse alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {

        return mapper.toResponse(
                service.alterarStatus(id, request.status())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}