package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.JogoRequest;
import com.curso.jogos.api.dto.JogoResponse;
import com.curso.jogos.api.mapper.JogoMapper;
import com.curso.jogos.api.dto.JogoAtualizacaoRequest;
import com.curso.jogos.api.dto.MovimentacaoEstoqueRequest;
import com.curso.jogos.api.dto.PaginaResponse;
import com.curso.jogos.api.dto.StatusRequest;
import com.curso.jogos.api.exception.ApiError;
import com.curso.jogos.application.JogoService;
import com.curso.jogos.domain.Jogo;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

@Tag(
        name = "Jogos",
        description = "Cadastro, consulta e movimentação de estoque dos jogos")
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

    @Operation(
            summary = "Cadastrar jogo",
            description = "Cria um jogo ativo associado a gênero e, opcionalmente, desenvolvedora e fornecedor.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Jogo cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Relacionamento informado não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Código do jogo já utilizado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<JogoResponse> cadastrar(
            @Valid @RequestBody JogoRequest request) {
        Jogo jogo = mapper.toEntity(request);

        Jogo cadastrado = service.cadastrar(
                jogo,
                request.generoId(),
                request.desenvolvedoraId(),
                request.fornecedorId());

        URI location = URI.create(
                "/api/jogos/" + cadastrado.getId());

        return ResponseEntity
                .created(location)
                .body(mapper.toResponse(cadastrado));
    }

    @Operation(
            summary = "Consultar jogo por ID",
            description = "Retorna a representação completa usada na visualização e na tela de alteração.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jogo encontrado"),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public JogoResponse buscarPorId(
            @Parameter(description = "Identificador do jogo", example = "1")
            @PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(
            summary = "Pesquisar jogos",
            description = "Combina filtros opcionais e executa paginação e ordenação no banco de dados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pesquisa executada"),
            @ApiResponse(responseCode = "400", description = "Paginação ou ordenação inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PaginaResponse<JogoResponse> pesquisar(
            @Parameter(description = "Trecho do título, sem diferenciar maiúsculas de minúsculas",
                    example = "baldur")
            @RequestParam(required = false) String titulo,
            @Parameter(description = "Status exato do jogo", example = "ATIVO")
            @RequestParam(required = false) Status status,
            @Parameter(description = "Identificador do gênero", example = "1")
            @RequestParam(required = false) Long generoId,
            @Parameter(description = "Identificador da desenvolvedora", example = "1")
            @RequestParam(required = false) Long desenvolvedoraId,
            @Parameter(description = "Identificador do fornecedor", example = "1")
            @RequestParam(required = false) Long fornecedorId,
            @Parameter(description = "Quando verdadeiro, retorna jogos abaixo do estoque mínimo",
                    example = "true")
            @RequestParam(required = false)
            Boolean abaixoEstoqueMinimo,
            @Parameter(description = "Parâmetros page, size e sort. A página começa em zero.")
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
                        fornecedorId,
                        abaixoEstoqueMinimo,
                        pageable
                ), mapper::toResponse
        );
    }

    @Operation(
            summary = "Alterar jogo",
            description = "Substitui título, preço, estoque mínimo, gênero, desenvolvedora e fornecedor. "
                    + "Código, quantidade disponível e status possuem fluxos próprios.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jogo alterado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Jogo, gênero ou desenvolvedora não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Operação incompatível com a integridade dos dados",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public JogoResponse alterar(
            @Parameter(description = "Identificador do jogo", example = "1")
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
                        request.desenvolvedoraId(),
                        request.fornecedorId()
                )
        );
    }

    @Operation(
            summary = "Ativar ou inativar jogo",
            description = "Executa uma transição explícita de status sem alterar os demais campos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado"),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/status")
    public JogoResponse alterarStatus(
            @Parameter(description = "Identificador do jogo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {

        return mapper.toResponse(
                service.alterarStatus(id, request.status())
        );
    }

    @Operation(
            summary = "Receber unidades",
            description = "Bloqueia o jogo e soma uma quantidade positiva ao estoque atual. "
                    + "Repetir a chamada repete o efeito.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrada registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/estoque/entradas")
    public JogoResponse receberUnidades(
            @Parameter(description = "Identificador do jogo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody
            MovimentacaoEstoqueRequest request) {

        return mapper.toResponse(
                service.receberUnidades(id, request.quantidade())
        );
    }

    @Operation(
            summary = "Retirar unidades",
            description = "Bloqueia o jogo e subtrai uma quantidade positiva sem permitir estoque negativo. "
                    + "Repetir a chamada repete o efeito.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saída registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida ou estoque insuficiente",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/estoque/saidas")
    public JogoResponse retirarUnidades(
            @Parameter(description = "Identificador do jogo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody
            MovimentacaoEstoqueRequest request) {

        return mapper.toResponse(
                service.retirarUnidades(id, request.quantidade())
        );
    }

    @Operation(
            summary = "Excluir jogo",
            description = "Exclui fisicamente o jogo informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Jogo excluído"),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Jogo referenciado por outro registro",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador do jogo", example = "1")
            @PathVariable Long id) {

        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
