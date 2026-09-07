package com.curso.jogos;

import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.domain.Jogo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class PersistenciaJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerGeneroEJogo() {
        GeneroJogo genero = new GeneroJogo("RPG");

        Jogo jogo = new Jogo(
                "RPG-001",
                "Baldur's Gate 3",
                5,
                new BigDecimal("199.90"),
                LocalDate.of(2026, 3, 10));

        genero.adicionarJogo(jogo);

        entityManager.persist(genero);
        entityManager.persist(jogo);
        entityManager.flush();

        Long jogoId = jogo.getId();
        entityManager.clear();

        Jogo recuperado = entityManager.find(Jogo.class, jogoId);

        assertNotNull(recuperado);
        assertEquals("Baldur's Gate 3", recuperado.getTitulo());
        assertEquals("RPG", recuperado.getGenero().getNome());
        assertEquals(0, recuperado.getEstoqueMinimo());
        assertNull(recuperado.getDesenvolvedora());
    }

    @Test
    void deveRegistrarDezesseteChangeSets() {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM databasechangelog",
                Long.class);

        assertEquals(17L, total);
    }

    @Test
    @Transactional
    void naoDevePermitirCodigoDuplicadoNoBanco() {
        Long generoId = inserirGeneroDiretamente("Estratégia");

        inserirJogoDiretamente(
                "EST-001",
                "Primeiro Jogo",
                2,
                generoId);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirJogoDiretamente(
                        "EST-001",
                        "Segundo jogo",
                        3,
                        generoId));
    }

    @Test
    @Transactional
    void naoDevePermitirCodigoDuplicadoDeDesenvolvedoraNoBanco() {
        inserirDesenvolvedoraDiretamente("Larian Studios", "LARIAN");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirDesenvolvedoraDiretamente(
                        "Outra desenvolvedora",
                        "LARIAN"));
    }

    @Test
    @Transactional
    void naoDevePermitirQuantidadeNegativaNoBanco() {
        Long generoId = inserirGeneroDiretamente("Estratégia");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirJogoDiretamente(
                        "AVN-001",
                        "Jogo inválido",
                        -1,
                        generoId));
    }

    private Long inserirGeneroDiretamente(String nome) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO genero_jogo (nome, status)
                VALUES (?, 'ATIVO')
                RETURNING id
                """,
                Long.class,
                nome);
    }

    private void inserirJogoDiretamente(
            String codigo,
            String titulo,
            int quantidade,
            Long generoId) {
        jdbcTemplate.update("""
                INSERT INTO jogo (
                    codigo_jogo,
                    titulo,
                    quantidade_disponivel,
                    preco_unitario,
                    estoque_minimo,
                    data_cadastro,
                    status,
                    genero_jogo_id
                )
                VALUES (?, ?, ?, 59.90, 0, DATE '2026-03-10', 'ATIVO', ?)
                """,
                codigo,
                titulo,
                quantidade,
                generoId);
    }

    private void inserirDesenvolvedoraDiretamente(String nome, String codigo) {
        jdbcTemplate.update("""
                INSERT INTO desenvolvedora (nome, codigo, status)
                VALUES (?, ?, 'ATIVO')
                """,
                nome,
                codigo);
    }
}
