package com.curso.jogos.application;

import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.repository.GeneroJogoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GeneroJogoService {
    private final GeneroJogoRepository repository;

    public GeneroJogoService(GeneroJogoRepository repository) {
        this.repository = repository;
    }


    @Transactional
    public GeneroJogo cadastrar(String nome) {
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new RecursoDuplicadoException("Nome do gênero já cadastrado");
        }

        return repository.save(new GeneroJogo(nome));
    }

    @Transactional(readOnly = true)
    public GeneroJogo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gênero de jogo não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<GeneroJogo> listar() {
        return repository.findAll();
    }
}