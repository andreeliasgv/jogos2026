package com.curso.jogos.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devePublicarContratoOpenApiComRotasEssenciais() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", startsWith("3.")))
                .andExpect(jsonPath("$.info.title")
                        .value("Jogos 2026 API"))
                .andExpect(jsonPath("$.info.version")
                        .value("1.0.0"))
                .andExpect(jsonPath(
                        "$.paths['/api/generos/{id}'].get")
                        .exists())
                .andExpect(jsonPath(
                        "$.paths['/api/generos/{id}'].put")
                        .exists())
                .andExpect(jsonPath(
                        "$.paths['/api/jogos/{id}'].get.summary")
                        .value("Consultar jogo por ID"))
                .andExpect(jsonPath(
                        "$.paths['/api/jogos/{id}'].get.responses['404'].content")
                        .exists())
                .andExpect(jsonPath(
                        "$.paths['/api/jogos/{id}'].delete")
                        .exists())
                .andExpect(jsonPath(
                        "$.paths['/api/jogos/{id}/estoque/entradas'].post")
                        .exists())
                .andExpect(jsonPath(
                        "$.paths['/api/jogos/{id}/estoque/saidas'].post")
                        .exists())
                .andExpect(jsonPath(
                        "$.components.schemas.JogoResponse")
                        .exists())
                .andExpect(jsonPath(
                        "$.components.schemas.ApiError")
                        .exists());
    }
}
