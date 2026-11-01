package com.curso.jogos.api.dto;

import com.curso.jogos.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Representação completa de um jogo")
public record JogoResponse(
        @Schema(description = "Identificador do jogo", example = "1")
        Long id,
        @Schema(description = "Código único do jogo", example = "JOGO-AVENTURA-001")
        String codigoJogo,
        @Schema(description = "Título comercial do jogo", example = "Aventura nas Estrelas")
        String titulo,
        @Schema(description = "Quantidade disponível atualmente", example = "10")
        int quantidadeDisponivel,
        @Schema(description = "Preço atual por unidade", example = "149.90")
        BigDecimal precoUnitario,
        @Schema(description = "Limite usado para indicar reposição", example = "2")
        int estoqueMinimo,
        @Schema(description = "Quantidade disponível multiplicada pelo preço unitário",
                example = "1499.00")
        BigDecimal valorInventario,
        @Schema(description = "Data de entrada do jogo no cadastro", example = "2026-10-31")
        LocalDate dataCadastro,
        @Schema(description = "Situação do jogo", example = "ATIVO")
        Status status,
        @Schema(description = "Identificador do gênero", example = "1")
        Long generoId,
        @Schema(description = "Nome do gênero", example = "RPG")
        String generoNome,
        @Schema(description = "Identificador da desenvolvedora, quando associada",
                example = "1", nullable = true)
        Long desenvolvedoraId,
        @Schema(description = "Nome da desenvolvedora, quando associada",
                example = "Estúdio Aurora", nullable = true)
        String desenvolvedoraNome,
        @Schema(description = "Identificador do fornecedor, quando associado",
                example = "1", nullable = true)
        Long fornecedorId,
        @Schema(description = "Razão social do fornecedor, quando associado",
                example = "Distribuidora Aurora Ltda.", nullable = true)
        String fornecedorRazaoSocial) {
}
