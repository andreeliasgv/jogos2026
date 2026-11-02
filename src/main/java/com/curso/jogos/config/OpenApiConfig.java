package com.curso.jogos.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI jogosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jogos 2026 API")
                        .description("API didática para gêneros, desenvolvedoras, fornecedores, "
                                + "pessoas, contas de acesso, clientes, colaboradores, jogos, "
                                + "vendas e estoque. Rotas protegidas usam JWT Bearer.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Projeto Jogos 2026")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .externalDocs(new ExternalDocumentation()
                        .description("Repositório e documentação do projeto")
                        .url("https://github.com/andreeliasgv/jogos2026"));
    }
}
