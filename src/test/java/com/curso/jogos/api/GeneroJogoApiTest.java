package com.curso.jogos.api;

import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.domain.Jogo;
import com.curso.jogos.repository.GeneroJogoRepository;
import com.curso.jogos.repository.JogoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class GeneroJogoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeneroJogoRepository generoRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Test
    void deveAlterarEInativarGenero() throws Exception {
        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("Gênero original"));

        mockMvc.perform(
                        put("/api/generos/{id}", genero.getId())
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nome": "Gênero alterado"
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome")
                        .value("Gênero alterado"))
                .andExpect(jsonPath("$.status")
                        .value("ATIVO"));

        mockMvc.perform(
                        put("/api/generos/{id}/status",
                                genero.getId())
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "INATIVO"
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("INATIVO"));
    }

    @Test
    void deveBuscarGeneroPorIdParaPreencherTelaDeAlteracao()
            throws Exception {

        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("Gênero para alteração"));

        mockMvc.perform(
                        get("/api/generos/{id}", genero.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(genero.getId()))
                .andExpect(jsonPath("$.nome")
                        .value("Gênero para alteração"))
                .andExpect(jsonPath("$.status")
                        .value("ATIVO"));
    }

    @Test
    void devePesquisarGenerosComPaginacao()
            throws Exception {

        generoRepository.save(
                new GeneroJogo("RPG eletrônico"));

        generoRepository.save(
                new GeneroJogo("Corrida"));

        mockMvc.perform(
                        get("/api/generos")
                                .param("nome", "rpg")
                                .param("page", "0")
                                .param("size", "5")
                                .param("sort", "nome,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()")
                        .value(1))
                .andExpect(jsonPath("$.conteudo[0].nome")
                        .value("RPG eletrônico"))
                .andExpect(jsonPath("$.pagina")
                        .value(0))
                .andExpect(jsonPath("$.tamanho")
                        .value(5))
                .andExpect(jsonPath("$.totalElementos")
                        .value(1));
    }

    @Test
    void deveExcluirGeneroSemJogos() throws Exception {
        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("Gênero descartável"));

        mockMvc.perform(
                        delete("/api/generos/{id}",
                                genero.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/generos/{id}",
                                genero.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoDeveExcluirGeneroQuePossuiJogos()
            throws Exception {

        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("Gênero em uso"));

        Jogo jogo = new Jogo(
                "GENERO-EM-USO-001",
                "Jogo relacionado",
                1,
                new BigDecimal("49.90"),
                0,
                LocalDate.of(2026, 10, 29));

        genero.adicionarJogo(jogo);
        jogoRepository.saveAndFlush(jogo);

        mockMvc.perform(
                        delete("/api/generos/{id}",
                                genero.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Gênero não pode ser excluído "
                                        + "porque possui jogos"));
    }
}
