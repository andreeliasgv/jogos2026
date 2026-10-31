package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.JogoRequest;
import com.curso.jogos.api.dto.JogoResponse;
import com.curso.jogos.api.mapper.JogoMapper;
import com.curso.jogos.api.dto.JogoAtualizacaoRequest;
import com.curso.jogos.api.dto.MovimentacaoEstoqueRequest;
import com.curso.jogos.api.dto.PaginaResponse;
import com.curso.jogos.api.dto.StatusRequest;
import com.curso.jogos.application.JogoService;
import com.curso.jogos.domain.Jogo;
import com.curso.jogos.domain.Status;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

@RestController
@RequestMapping("/api/jogos")
public class JogoController {

    private final JogoService service;
    private final JogoMapper mapper;

    public JogoController(
            JogoService service,
            JogoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<JogoResponse> cadastrar(
            @Valid @RequestBody JogoRequest request) {
        Jogo jogo = mapper.toEntity(request);

        Jogo cadastrado = service.cadastrar(
                jogo,
                request.generoId(),
                request.desenvolvedoraId());

        URI location = URI.create(
                "/api/jogos/" + cadastrado.getId());

        return ResponseEntity
                .created(location)
                .body(mapper.toResponse(cadastrado));
    }

    @GetMapping("/{id}")
    public JogoResponse buscarPorId(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @GetMapping
    public PaginaResponse<JogoResponse> pesquisar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Long generoId,
            @RequestParam(required = false) Long desenvolvedoraId,
            @RequestParam(required = false)
            Boolean abaixoEstoqueMinimo,
            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        return PaginaResponse.de(
                service.pesquisar(
                        titulo,
                        status,
                        generoId,
                        desenvolvedoraId,
                        abaixoEstoqueMinimo,
                        pageable
                ), mapper::toResponse
        );
    }

    @PutMapping("/{id}")
    public JogoResponse alterar(
            @PathVariable Long id,
            @Valid @RequestBody
            JogoAtualizacaoRequest request) {

        return mapper.toResponse(
                service.alterar(
                        id,
                        request.titulo(),
                        request.precoUnitario(),
                        request.estoqueMinimo(),
                        request.generoId(),
                        request.desenvolvedoraId()
                )
        );
    }

    @PutMapping("/{id}/status")
    public JogoResponse alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {

        return mapper.toResponse(
                service.alterarStatus(id, request.status())
        );
    }

    @PostMapping("/{id}/estoque/entradas")
    public JogoResponse receberUnidades(
            @PathVariable Long id,
            @Valid @RequestBody
            MovimentacaoEstoqueRequest request) {

        return mapper.toResponse(
                service.receberUnidades(id, request.quantidade())
        );
    }

    @PostMapping("/{id}/estoque/saidas")
    public JogoResponse retirarUnidades(
            @PathVariable Long id,
            @Valid @RequestBody
            MovimentacaoEstoqueRequest request) {

        return mapper.toResponse(
                service.retirarUnidades(id, request.quantidade())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}