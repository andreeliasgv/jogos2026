package com.curso.jogos.api.dto;

import com.curso.jogos.domain.PerfilAcesso;
import com.curso.jogos.domain.StatusContaAcesso;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Conta de autenticação sem exposição de senha ou hash")
public record ContaAcessoResponse(
        Long id,
        Long pessoaId,
        String nomePessoa,
        String login,
        StatusContaAcesso status,
        Set<PerfilAcesso> perfis) {
}
