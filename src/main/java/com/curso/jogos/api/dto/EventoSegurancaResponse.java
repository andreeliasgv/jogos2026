package com.curso.jogos.api.dto;

import com.curso.jogos.domain.TipoEventoSeguranca;
import java.time.Instant;

public record EventoSegurancaResponse(
        Long id, TipoEventoSeguranca tipo, Instant ocorridoEm,
        Long atorContaId, Long alvoContaId, String enderecoOrigem) {
}
