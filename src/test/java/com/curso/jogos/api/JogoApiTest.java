package com.curso.jogos.api;

import com.curso.jogos.domain.Desenvolvedora;
import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.domain.Jogo;
import com.curso.jogos.repository.DesenvolvedoraRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class JogoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeneroJogoRepository generoRepository;

    @Autowired
    private DesenvolvedoraRepository desenvolvedoraRepository;

    @Autowired
    private JogoRepository jogoRepository;

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
    void devePesquisarJogosPaginadosERetornar200() throws Exception {
        mockMvc.perform(get("/api/jogos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").isArray())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(20))
                .andExpect(jsonPath("$.totalElementos").value(0))
                .andExpect(jsonPath("$.totalPaginas").value(0))
                .andExpect(jsonPath("$.primeira").value(true))
                .andExpect(jsonPath("$.ultima").value(true));
    }

    @Test
    void deveBuscarJogoPorIdParaPreencherTelaDeAlteracao()
            throws Exception {

        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("Gênero formulário"));

        Desenvolvedora desenvolvedora =
                desenvolvedoraRepository.save(
                        new Desenvolvedora(
                                "Desenvolvedora formulário",
                                "DEV-FORMULARIO"));

        Jogo jogo = new Jogo(
                "API-FORMULARIO-001",
                "Jogo para alteração",
                10,
                new BigDecimal("49.90"),
                2,
                LocalDate.of(2026, 10, 29));

        genero.adicionarJogo(jogo);
        jogo.associarDesenvolvedora(desenvolvedora);
        jogoRepository.save(jogo);

        mockMvc.perform(
                        get("/api/jogos/{id}", jogo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(jogo.getId()))
                .andExpect(jsonPath("$.codigoJogo")
                        .value("API-FORMULARIO-001"))
                .andExpect(jsonPath("$.titulo")
                        .value("Jogo para alteração"))
                .andExpect(jsonPath("$.quantidadeDisponivel")
                        .value(10))
                .andExpect(jsonPath("$.precoUnitario")
                        .value(49.90))
                .andExpect(jsonPath("$.estoqueMinimo")
                        .value(2))
                .andExpect(jsonPath("$.status")
                        .value("ATIVO"))
                .andExpect(jsonPath("$.generoId")
                        .value(genero.getId()))
                .andExpect(jsonPath("$.generoNome")
                        .value("Gênero formulário"))
                .andExpect(jsonPath("$.desenvolvedoraId")
                        .value(desenvolvedora.getId()))
                .andExpect(jsonPath("$.desenvolvedoraNome")
                        .value("Desenvolvedora formulário"));
    }

    @Test
    void deveAlterarStatusEMovimentarEstoque()
            throws Exception {

        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("Gênero operações"));

        Jogo jogo = salvarJogo(
                genero,
                "API-OPERACOES-001",
                "Jogo original");

        String alteracao = """
            {
              "titulo": "Jogo alterado",
              "precoUnitario": 59.90,
              "estoqueMinimo": 4,
              "generoId": %d,
              "desenvolvedoraId": null
            }
            """.formatted(genero.getId());

        mockMvc.perform(
                        put("/api/jogos/{id}", jogo.getId())
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(alteracao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo")
                        .value("Jogo alterado"))
                .andExpect(jsonPath("$.precoUnitario")
                        .value(59.90))
                .andExpect(jsonPath("$.estoqueMinimo")
                        .value(4));

        mockMvc.perform(
                        put("/api/jogos/{id}/status",
                                jogo.getId())
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

        mockMvc.perform(
                        post("/api/jogos/{id}/estoque/entradas",
                                jogo.getId())
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "quantidade": 3
                                    }
                                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeDisponivel")
                        .value(13));

        mockMvc.perform(
                        post("/api/jogos/{id}/estoque/saidas",
                                jogo.getId())
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "quantidade": 2
                                    }
                                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeDisponivel")
                        .value(11));
    }

    @Test
    void devePesquisarJogosComFiltrosEPaginacao()
            throws Exception {

        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("Gênero pesquisa"));

        salvarJogo(
                genero,
                "API-PESQUISA-001",
                "Baldurs Gate");

        salvarJogo(
                genero,
                "API-PESQUISA-002",
                "Forza Horizon");

        mockMvc.perform(
                        get("/api/jogos")
                                .param("titulo", "gate")
                                .param(
                                        "generoId",
                                        genero.getId().toString())
                                .param("page", "0")
                                .param("size", "5")
                                .param("sort", "titulo,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()")
                        .value(1))
                .andExpect(jsonPath("$.conteudo[0].titulo")
                        .value("Baldurs Gate"))
                .andExpect(jsonPath("$.totalElementos")
                        .value(1))
                .andExpect(jsonPath("$.pagina")
                        .value(0))
                .andExpect(jsonPath("$.tamanho")
                        .value(5));
    }

    @Test
    void deveExcluirJogo() throws Exception {
        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("Gênero exclusão"));

        Jogo jogo = salvarJogo(
                genero,
                "API-EXCLUSAO-001",
                "Jogo descartável");

        mockMvc.perform(
                        delete("/api/jogos/{id}", jogo.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/jogos/{id}", jogo.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoDeveRetirarQuantidadeMaiorQueADisponivel()
            throws Exception {

        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("Gênero estoque insuficiente"));

        Jogo jogo = salvarJogo(
                genero,
                "API-ESTOQUE-INSUFICIENTE-001",
                "Jogo com estoque limitado");

        mockMvc.perform(
                        post("/api/jogos/{id}/estoque/saidas",
                                jogo.getId())
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "quantidade": 11
                                    }
                                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Quantidade disponível insuficiente"));
    }

    @Test
    void deveRejeitarCampoDeOrdenacaoNaoPermitido()
            throws Exception {

        mockMvc.perform(
                        get("/api/jogos")
                                .param(
                                        "sort",
                                        "desenvolvedora.nome,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Campo de ordenação inválido: "
                                        + "desenvolvedora.nome"));
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

    private Jogo salvarJogo(
            GeneroJogo genero,
            String codigo,
            String titulo) {

        Jogo jogo = new Jogo(
                codigo,
                titulo,
                10,
                new BigDecimal("49.90"),
                2,
                LocalDate.of(2026, 10, 29));

        genero.adicionarJogo(jogo);
        return jogoRepository.save(jogo);
    }
}
