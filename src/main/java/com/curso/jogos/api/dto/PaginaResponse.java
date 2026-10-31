package com.curso.jogos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;


@Schema(description = "Página de resultados acompanhada dos metadados de navegação")
public record PaginaResponse<T>(
        @Schema(description = "Elementos encontrados na página atual")
        List<T> conteudo,
        @Schema(description = "Número da página atual, iniciado em zero", example = "0")
        int pagina,
        @Schema(description = "Quantidade máxima de elementos por página", example = "20")
        int tamanho,
        @Schema(description = "Quantidade total de elementos encontrados", example = "42")
        long totalElementos,
        @Schema(description = "Quantidade total de páginas", example = "3")
        int totalPaginas,
        @Schema(description = "Indica se esta é a primeira página", example = "true")
        boolean primeira,
        @Schema(description = "Indica se esta é a última página", example = "false")
        boolean ultima) {
    public static <S, T> PaginaResponse<T> de(
            Page<S> pagina,
            Function<S, T> conversor) {
        return new PaginaResponse<>(
                pagina.getContent().stream().map(conversor).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isFirst(),
                pagina.isLast());
    }
}
