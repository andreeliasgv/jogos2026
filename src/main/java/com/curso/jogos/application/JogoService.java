package com.curso.jogos.application;

import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.domain.Jogo;
import com.curso.jogos.repository.GeneroJogoRepository;
import com.curso.jogos.repository.JogoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JogoService {
    private final JogoRepository jogoRepository;
    private final GeneroJogoRepository generoRepository;

    public JogoService(
            JogoRepository jogoRepository,
            GeneroJogoRepository generoRepository) {
        this.jogoRepository = jogoRepository;
        this.generoRepository = generoRepository;
    }

    @Transactional
    public Jogo cadastrar(Jogo jogo, Long generoId) {
        if (jogoRepository.existsByCodigoJogo(
                jogo.getCodigoJogo())) {
            throw new RecursoDuplicadoException("Código do jogo já cadastrado");
        }

        GeneroJogo genero = generoRepository.findById(generoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Gênero de jogo não encontrado"));

        genero.adicionarJogo(jogo);

        return jogoRepository.save(jogo);
    }

    @Transactional
    public Jogo receberUnidades(Long id, int quantidade) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));

        jogo.receberUnidades(quantidade);

        return jogo;
    }
}