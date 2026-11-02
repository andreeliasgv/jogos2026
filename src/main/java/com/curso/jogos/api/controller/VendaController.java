package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.ItemVendaRequest;
import com.curso.jogos.api.dto.PaginaResponse;
import com.curso.jogos.api.dto.VendaRequest;
import com.curso.jogos.api.dto.VendaResponse;
import com.curso.jogos.api.dto.VendaResumoResponse;
import com.curso.jogos.api.exception.ApiError;
import com.curso.jogos.api.mapper.VendaMapper;
import com.curso.jogos.application.VendaService;
import com.curso.jogos.domain.StatusVenda;
import com.curso.jogos.domain.Venda;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;

@Tag(name = "Vendas", description = "Itens, preço histórico e baixa transacional de estoque")
@RestController
@RequestMapping("/api/vendas")
public class VendaController {
    private final VendaService service;
    private final VendaMapper mapper;

    public VendaController(VendaService service, VendaMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Abrir venda", description = "Cria uma venda aberta para cliente e vendedor ativos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venda aberta"),
            @ApiResponse(responseCode = "400", description = "Participante inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Participante não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<VendaResponse> cadastrar(@Valid @RequestBody VendaRequest request) {
        Venda venda = service.cadastrar(request.clienteId(), request.vendedorId());
        return ResponseEntity.created(URI.create("/api/vendas/" + venda.getId()))
                .body(mapper.toResponse(venda));
    }

    @Operation(summary = "Consultar venda por ID")
    @GetMapping("/{id}")
    public VendaResponse buscar(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(summary = "Pesquisar vendas", description = "Filtra por status, participantes e datas.")
    @GetMapping
    public PaginaResponse<VendaResumoResponse> pesquisar(
            @RequestParam(required = false) StatusVenda status,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long vendedorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @PageableDefault(size = 20, sort = "dataVenda", direction = Sort.Direction.DESC) Pageable pageable) {
        return PaginaResponse.de(service.pesquisar(status, clienteId, vendedorId,
                dataInicial, dataFinal, pageable), mapper::toResumo);
    }

    @Operation(summary = "Adicionar item", description = "Captura o preço atual do jogo na venda aberta.")
    @PostMapping("/{id}/itens")
    public VendaResponse adicionarItem(@PathVariable Long id,
                                       @Valid @RequestBody ItemVendaRequest request) {
        return mapper.toResponse(service.adicionarItem(id, request.jogoId(), request.quantidade()));
    }

    @Operation(summary = "Remover item", description = "Remove um item enquanto a venda estiver aberta.")
    @DeleteMapping("/{vendaId}/itens/{itemId}")
    public VendaResponse removerItem(@PathVariable Long vendaId, @PathVariable Long itemId) {
        return mapper.toResponse(service.removerItem(vendaId, itemId));
    }

    @Operation(
            summary = "Finalizar venda",
            description = "Valida toda a venda, bloqueia os jogos em ordem determinística "
                    + "e baixa o estoque na mesma transação.")
    @PostMapping("/{id}/finalizacao")
    public VendaResponse finalizar(@PathVariable Long id) {
        return mapper.toResponse(service.finalizar(id));
    }

    @Operation(summary = "Cancelar venda", description = "Cancela uma venda aberta sem baixa de estoque.")
    @PostMapping("/{id}/cancelamento")
    public VendaResponse cancelar(@PathVariable Long id) {
        return mapper.toResponse(service.cancelar(id));
    }
}
