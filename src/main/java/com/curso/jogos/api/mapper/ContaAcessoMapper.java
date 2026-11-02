package com.curso.jogos.api.mapper;

import com.curso.jogos.api.dto.ContaAcessoResponse;
import com.curso.jogos.domain.ContaAcesso;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;

@Component
public class ContaAcessoMapper {
    public ContaAcessoResponse toResponse(ContaAcesso conta) {
        return new ContaAcessoResponse(
                conta.getId(),
                conta.getPessoa().getId(),
                conta.getPessoa().getNome(),
                conta.getLogin(),
                conta.getStatus(),
                new LinkedHashSet<>(conta.getPerfis()));
    }
}
