package com.curso.jogos.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @Operation(summary = "Verificar saúde da aplicação")
    @SecurityRequirements
    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }
}
