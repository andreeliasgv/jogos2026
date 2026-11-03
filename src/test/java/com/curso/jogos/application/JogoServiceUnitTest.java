package com.curso.jogos.application;

import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.domain.Jogo;
import com.curso.jogos.repository.DesenvolvedoraRepository;
import com.curso.jogos.repository.FornecedorRepository;
import com.curso.jogos.repository.GeneroJogoRepository;
import com.curso.jogos.repository.JogoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JogoServiceUnitTest {

    @Mock
    JogoRepository jogoRepository;

    @Mock
    GeneroJogoRepository generoRepository;

    @Mock
    DesenvolvedoraRepository desenvolvedoraRepository;

    @Mock
    FornecedorRepository fornecedorRepository;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    JogoService service;

    @Test
    void deveCadastrarJogoAssociandoGenero() {
        Jogo jogo = novoJogo("UNIT-001");
        GeneroJogo genero = new GeneroJogo("RPG unitário");
        when(jogoRepository.existsByCodigoJogo("UNIT-001")).thenReturn(false);
        when(generoRepository.findById(1L)).thenReturn(Optional.of(genero));
        when(jogoRepository.save(jogo)).thenReturn(jogo);

        Jogo cadastrado = service.cadastrar(jogo, 1L);

        assertSame(jogo, cadastrado);
        assertSame(genero, cadastrado.getGenero());
        assertEquals(1, genero.getJogos().size());
        verify(jogoRepository).save(jogo);
    }

    @Test
    void naoDeveConsultarRelacionamentosQuandoCodigoJaExiste() {
        Jogo jogo = novoJogo("UNIT-DUPLICADO");
        when(jogoRepository.existsByCodigoJogo("UNIT-DUPLICADO")).thenReturn(true);

        RecursoDuplicadoException erro = assertThrows(
                RecursoDuplicadoException.class,
                () -> service.cadastrar(jogo, 1L));

        assertEquals("Código do jogo já cadastrado", erro.getMessage());
        verifyNoInteractions(
                generoRepository,
                desenvolvedoraRepository,
                fornecedorRepository,
                entityManager);
        verify(jogoRepository, never()).save(jogo);
    }

    @Test
    void naoDeveSalvarQuandoGeneroNaoExiste() {
        Jogo jogo = novoJogo("UNIT-SEM-GENERO");
        when(jogoRepository.existsByCodigoJogo("UNIT-SEM-GENERO")).thenReturn(false);
        when(generoRepository.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException erro = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.cadastrar(jogo, 999L));

        assertEquals("Gênero de jogo não encontrado", erro.getMessage());
        verify(jogoRepository, never()).save(jogo);
    }

    @Test
    void deveReceberUnidadesDepoisDeAdquirirLock() {
        Jogo jogo = novoJogo("UNIT-ENTRADA");
        when(jogoRepository.buscarPorIdComRelacionamentos(1L))
                .thenReturn(Optional.of(jogo));

        Jogo atualizado = service.receberUnidades(1L, 5);

        assertEquals(15, atualizado.getQuantidadeDisponivel());
        verify(entityManager).refresh(jogo, LockModeType.PESSIMISTIC_WRITE);
        verify(jogoRepository, never()).save(jogo);
    }

    @Test
    void naoDeveRetirarQuantidadeMaiorQueDisponivel() {
        Jogo jogo = novoJogo("UNIT-SAIDA");
        when(jogoRepository.buscarPorIdComRelacionamentos(1L))
                .thenReturn(Optional.of(jogo));

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.retirarUnidades(1L, 11));

        assertEquals("Quantidade disponível insuficiente", erro.getMessage());
        assertEquals(10, jogo.getQuantidadeDisponivel());
        verify(entityManager).refresh(jogo, LockModeType.PESSIMISTIC_WRITE);
    }

    private Jogo novoJogo(String codigo) {
        return new Jogo(
                codigo,
                "Jogo de teste unitário",
                10,
                new BigDecimal("25.90"),
                LocalDate.of(2026, 11, 3));
    }
}
