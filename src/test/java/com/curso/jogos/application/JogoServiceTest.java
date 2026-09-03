package com.curso.jogos.application;

import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.domain.Jogo;
import com.curso.jogos.repository.GeneroJogoRepository;
import com.curso.jogos.repository.JogoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class JogoServiceTest {
    @Autowired
    private JogoService jogoService;

    @Autowired
    private GeneroJogoRepository generoRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveCadastrarJogoComGenero() {
        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("RPG de teste"));

        Jogo cadastrado = jogoService.cadastrar(
                novoJogo("TESTE-SERVICE-001"),
                genero.getId());
        assertNotNull(cadastrado.getId());
        assertEquals(
                genero.getId(),
                cadastrado.getGenero().getId());
    }

    @Test
    void deveImpedirCodigoDeJogoDuplicado() {
        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("Gênero duplicidade"));

        jogoService.cadastrar(
                novoJogo("TESTE-DUPLICADO"),
                genero.getId());

        assertThrows(
                RecursoDuplicadoException.class,
                () -> jogoService.cadastrar(
                        novoJogo("TESTE-DUPLICADO"),
                        genero.getId()));
    }

    @Test
    void deveInformarGeneroInexistenteSemSalvarJogo() {
        Jogo jogo = novoJogo("TESTE-SEM-GENERO");

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> jogoService.cadastrar(jogo, Long.MAX_VALUE));

        assertFalse(jogoRepository.existsByCodigoJogo(jogo.getCodigoJogo()));
    }

    @Test
    void deveAtualizarQuantidadePorDirtyChecking() {
        GeneroJogo genero = generoRepository.save(
                new GeneroJogo("Gênero dirty checking"));

        Jogo jogo = jogoService.cadastrar(
                novoJogo("TESTE-DIRTY-CHECKING"),
                genero.getId());

        jogoService.receberUnidades(jogo.getId(), 5);

        entityManager.flush();
        entityManager.clear();

        Jogo atualizado = jogoRepository.findById(jogo.getId())
                .orElseThrow();

        assertEquals(15, atualizado.getQuantidadeDisponivel());
    }

    private Jogo novoJogo(String codigo) {
        return new Jogo(
                codigo,
                "Jogo de teste",
                10,
                new BigDecimal("25.90"),
                LocalDate.of(2026, 9, 2));
    }
}