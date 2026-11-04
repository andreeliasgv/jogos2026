package com.curso.jogos.api.controller;

import com.curso.jogos.api.dto.EventoSegurancaResponse;
import com.curso.jogos.api.dto.PaginaResponse;
import com.curso.jogos.application.AuditoriaSegurancaService;
import com.curso.jogos.domain.EventoSeguranca;
import com.curso.jogos.domain.TipoEventoSeguranca;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auditoria de segurança",
        description = "Consulta administrativa de eventos de segurança")
@RestController
@RequestMapping("/api/auditoria-seguranca")
public class AuditoriaSegurancaController {
    private final AuditoriaSegurancaService service;

    public AuditoriaSegurancaController(AuditoriaSegurancaService service) {
        this.service = service;
    }

    @Operation(summary = "Consultar eventos de segurança",
            description = "Exige o perfil ADMINISTRADOR")
    @GetMapping
    public PaginaResponse<EventoSegurancaResponse> consultar(
            @RequestParam(required = false) TipoEventoSeguranca tipo,
            Pageable pageable) {
        return PaginaResponse.de(service.consultar(tipo, pageable), this::toResponse);
    }

    private EventoSegurancaResponse toResponse(EventoSeguranca evento) {
        return new EventoSegurancaResponse(evento.getId(), evento.getTipo(),
                evento.getOcorridoEm(), evento.getAtorContaId(),
                evento.getAlvoContaId(), evento.getEnderecoOrigem());
    }
}
