package com.curso.jogos.api.mapper;

import com.curso.jogos.api.dto.JogoRequest;
import com.curso.jogos.api.dto.JogoResponse;
import com.curso.jogos.domain.Desenvolvedora;
import com.curso.jogos.domain.Jogo;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class JogoMapper {

    public Jogo toEntity(JogoRequest request) {
        return new Jogo(
                request.codigoJogo(),
                request.titulo(),
                request.quantidadeDisponivel(),
                request.precoUnitario(),
                request.estoqueMinimo(),
                LocalDate.now());
    }

    public JogoResponse toResponse(Jogo jogo) {
        Desenvolvedora desenvolvedora = jogo.getDesenvolvedora();

        return new JogoResponse(
                jogo.getId(),
                jogo.getCodigoJogo(),
                jogo.getTitulo(),
                jogo.getQuantidadeDisponivel(),
                jogo.getPrecoUnitario(),
                jogo.getEstoqueMinimo(),
                jogo.calcularValorInventario(),
                jogo.getDataCadastro(),
                jogo.getStatus(),
                jogo.getGenero().getId(),
                jogo.getGenero().getNome(),
                desenvolvedora == null ? null : desenvolvedora.getId(),
                desenvolvedora == null ? null : desenvolvedora.getNome());
    }
}