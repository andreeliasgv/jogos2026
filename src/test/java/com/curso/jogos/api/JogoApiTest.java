package com.curso.jogos.api;

import com.curso.jogos.domain.Desenvolvedora;
import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.repository.DesenvolvedoraRepository;
import com.curso.jogos.repository.GeneroJogoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JogoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeneroJogoRepository generoRepository;

    @Autowired
    private DesenvolvedoraRepository desenvolvedoraRepository;

    @Test
    void deveCadastrarGeneroERetornar201() throws Exception {
        mockMvc.perform(post("/api/generos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Aventura API"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Aventura API"))
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void deveCadastrarDesenvolvedoraERetornar201() throws Exception {
        mockMvc.perform(post("/api/desenvolvedoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Larian Studios API",
                                  "codigo": "larian-api"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Larian Studios API"))
                .andExpect(jsonPath("$.codigo").value("LARIAN-API"))
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void deveCadastrarJogoERetornar201() throws Exception {
        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("RPG API"));

        Desenvolvedora desenvolvedora = desenvolvedoraRepository.save(
                new Desenvolvedora("Desenvolvedora API", "DEV-API"));

        mockMvc.perform(post("/api/jogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonJogo(
                                "API-JOGO-001",
                                genero.getId(),
                                desenvolvedora.getId())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.codigoJogo").value("API-JOGO-001"))
                .andExpect(jsonPath("$.titulo").value("Jogo criado pela API"))
                .andExpect(jsonPath("$.estoqueMinimo").value(2))
                .andExpect(jsonPath("$.generoNome").value("RPG API"))
                .andExpect(jsonPath("$.desenvolvedoraNome")
                        .value("Desenvolvedora API"));
    }

    @Test
    void deveRetornar400ComErrosPorCampo() throws Exception {
        mockMvc.perform(post("/api/jogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Um ou mais campos são inválidos"))
                .andExpect(jsonPath("$.fields.codigoJogo").exists())
                .andExpect(jsonPath("$.fields.titulo").exists())
                .andExpect(jsonPath("$.fields.generoId").exists());
    }

    @Test
    void deveRetornar404ParaJogoInexistente() throws Exception {
        mockMvc.perform(get("/api/jogos/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Jogo não encontrado"))
                .andExpect(jsonPath("$.path")
                        .value("/api/jogos/" + Long.MAX_VALUE));
    }

    @Test
    void deveRetornar409ParaCodigoDuplicado() throws Exception {
        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("Gênero duplicidade API"));

        String json = jsonJogo(
                "API-DUPLICADO",
                genero.getId(),
                null);

        mockMvc.perform(post("/api/jogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/jogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Código do jogo já cadastrado"));
    }

    @Test
    void deveRetornar400ParaJsonMalformado() throws Exception {
        mockMvc.perform(post("/api/jogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoJogo": "JSON-INVALIDO"
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("JSON ausente ou inválido"));
    }

    @Test
    void deveListarJogosERetornar200() throws Exception {
        mockMvc.perform(get("/api/jogos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private String jsonJogo(
            String codigo,
            Long generoId,
            Long desenvolvedoraId) {
        String campoDesenvolvedora = desenvolvedoraId == null
                ? "null"
                : desenvolvedoraId.toString();

        return """
                {
                  "codigoJogo": "%s",
                  "titulo": "Jogo criado pela API",
                  "quantidadeDisponivel": 10,
                  "precoUnitario": 89.90,
                  "estoqueMinimo": 2,
                  "generoId": %d,
                  "desenvolvedoraId": %s
                }
                """.formatted(
                codigo,
                generoId,
                campoDesenvolvedora);
    }
}