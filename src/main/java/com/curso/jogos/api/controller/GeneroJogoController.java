package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.GeneroJogoAtualizacaoRequest;
import com.curso.jogos.api.dto.GeneroJogoRequest;
import com.curso.jogos.api.dto.StatusRequest;
import com.curso.jogos.api.dto.GeneroJogoResponse;
import com.curso.jogos.api.dto.PaginaResponse;
import com.curso.jogos.api.exception.ApiError;
import com.curso.jogos.api.mapper.GeneroJogoMapper;
import com.curso.jogos.application.GeneroJogoService;
import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.domain.Status;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.net.URI;

@Tag(
        name = "Gêneros de jogos",
        description = "Classificação, consulta e situação dos jogos")
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

    @Operation(
            summary = "Cadastrar gênero",
            description = "Cria um gênero ativo e devolve sua localização.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Gênero cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Nome já utilizado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<GeneroJogoResponse> cadastrar(
            @Valid @RequestBody GeneroJogoRequest request) {
        GeneroJogo genero = service.cadastrar(request.nome());
        URI location = URI.create("/api/generos/" + genero.getId());

        return ResponseEntity
                .created(location)
                .body(mapper.toResponse(genero));
    }

    @Operation(
            summary = "Consultar gênero por ID",
            description = "Retorna os dados usados na visualização e na tela de alteração.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gênero encontrado"),
            @ApiResponse(responseCode = "404", description = "Gênero não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public GeneroJogoResponse buscarPorId(
            @Parameter(description = "Identificador do gênero", example = "1")
            @PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(
            summary = "Pesquisar gêneros",
            description = "Combina filtros opcionais e devolve uma página ordenável.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pesquisa executada"),
            @ApiResponse(responseCode = "400", description = "Paginação ou ordenação inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PaginaResponse<GeneroJogoResponse> pesquisar(
            @Parameter(description = "Trecho do nome, sem diferenciar maiúsculas de minúsculas",
                    example = "aventura")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Status exato do gênero", example = "ATIVO")
            @RequestParam(required = false) Status status,
            @Parameter(description = "Parâmetros page, size e sort. A página começa em zero.")
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

    @Operation(
            summary = "Alterar gênero",
            description = "Substitui os campos editáveis. O status possui uma operação própria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gênero alterado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Gênero não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Nome já utilizado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public GeneroJogoResponse alterar(
            @Parameter(description = "Identificador do gênero", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody
            GeneroJogoAtualizacaoRequest request) {

        return mapper.toResponse(
                service.alterar(id, request.nome())
        );
    }

    @Operation(
            summary = "Ativar ou inativar gênero",
            description = "Executa uma transição explícita de status sem alterar os demais campos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado"),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Gênero não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/status")
    public GeneroJogoResponse alterarStatus(
            @Parameter(description = "Identificador do gênero", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {

        return mapper.toResponse(
                service.alterarStatus(id, request.status())
        );
    }

    @Operation(
            summary = "Excluir gênero",
            description = "Exclui fisicamente somente quando nenhum jogo utiliza o gênero.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Gênero excluído"),
            @ApiResponse(responseCode = "404", description = "Gênero não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Gênero em uso por jogos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador do gênero", example = "1")
            @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
