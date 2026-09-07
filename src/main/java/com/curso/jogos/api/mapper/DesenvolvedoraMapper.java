package com.curso.jogos.api.mapper;

import com.curso.jogos.api.dto.DesenvolvedoraRequest;
import com.curso.jogos.api.dto.DesenvolvedoraResponse;
import com.curso.jogos.domain.Desenvolvedora;
import org.springframework.stereotype.Component;

@Component
public class DesenvolvedoraMapper {

    public Desenvolvedora toEntity(DesenvolvedoraRequest request) {
        return new Desenvolvedora(
                request.nome(),
                request.codigo());
    }

    public DesenvolvedoraResponse toResponse(Desenvolvedora desenvolvedora) {
        return new DesenvolvedoraResponse(
                desenvolvedora.getId(),
                desenvolvedora.getNome(),
                desenvolvedora.getCodigo(),
                desenvolvedora.getStatus());
    }
}