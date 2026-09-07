package com.curso.jogos.application;

import com.curso.jogos.domain.Desenvolvedora;
import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.domain.Jogo;
import com.curso.jogos.repository.DesenvolvedoraRepository;
import com.curso.jogos.repository.GeneroJogoRepository;
import com.curso.jogos.repository.JogoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JogoService {

    private final JogoRepository jogoRepository;
    private final GeneroJogoRepository generoRepository;
    private final DesenvolvedoraRepository desenvolvedoraRepository;

    public JogoService(
            JogoRepository jogoRepository,
            GeneroJogoRepository generoRepository,
            DesenvolvedoraRepository desenvolvedoraRepository) {
        this.jogoRepository = jogoRepository;
        this.generoRepository = generoRepository;
        this.desenvolvedoraRepository = desenvolvedoraRepository;
    }

    @Transactional
    public Jogo cadastrar(Jogo jogo, Long generoId) {
        return cadastrar(jogo, generoId, null);
    }

    @Transactional
    public Jogo cadastrar(
            Jogo jogo,
            Long generoId,
            Long desenvolvedoraId) {
        if (jogoRepository.existsByCodigoJogo(jogo.getCodigoJogo())) {
            throw new RecursoDuplicadoException(
                    "Código do jogo já cadastrado");
        }

        GeneroJogo genero = generoRepository.findById(generoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Gênero de jogo não encontrado"));

        genero.adicionarJogo(jogo);

        if (desenvolvedoraId != null) {
            Desenvolvedora desenvolvedora =
                    desenvolvedoraRepository.findById(desenvolvedoraId)
                            .orElseThrow(() ->
                                    new RecursoNaoEncontradoException(
                                            "Desenvolvedora não encontrada"));

            jogo.associarDesenvolvedora(desenvolvedora);
        }

        return jogoRepository.save(jogo);
    }

    @Transactional(readOnly = true)
    public Jogo buscarPorId(Long id) {
        return jogoRepository.buscarPorIdComRelacionamentos(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Jogo não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Jogo> listar() {
        return jogoRepository.buscarTodosComRelacionamentos();
    }

    @Transactional
    public Jogo receberUnidades(Long id, int quantidade) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Jogo não encontrado"));

        jogo.receberUnidades(quantidade);
        return jogo;
    }
}