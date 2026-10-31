package com.curso.jogos.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI jogosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jogos 2026 API")
                        .description("API didática para gerenciamento de gêneros, "
                                + "desenvolvedoras, jogos e movimentações de estoque.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Projeto Jogos 2026")))
                .externalDocs(new ExternalDocumentation()
                        .description("Repositório e documentação do projeto")
                        .url("https://github.com/andreeliasgv/jogos2026"));
    }
}
