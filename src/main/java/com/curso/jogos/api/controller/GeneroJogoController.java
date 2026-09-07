package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.GeneroJogoRequest;
import com.curso.jogos.api.dto.GeneroJogoResponse;
import com.curso.jogos.api.mapper.GeneroJogoMapper;
import com.curso.jogos.application.GeneroJogoService;
import com.curso.jogos.domain.GeneroJogo;
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
    public List<GeneroJogoResponse> listar() {
        return service.listar()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}