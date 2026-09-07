package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.DesenvolvedoraRequest;
import com.curso.jogos.api.dto.DesenvolvedoraResponse;
import com.curso.jogos.api.mapper.DesenvolvedoraMapper;
import com.curso.jogos.application.DesenvolvedoraService;
import com.curso.jogos.domain.Desenvolvedora;
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

    @GetMapping("/{id}")
    public DesenvolvedoraResponse buscarPorId(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @GetMapping
    public List<DesenvolvedoraResponse> listar() {
        return service.listar()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}