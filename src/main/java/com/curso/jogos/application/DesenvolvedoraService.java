package com.curso.jogos.application;

import com.curso.jogos.domain.Desenvolvedora;
import com.curso.jogos.repository.DesenvolvedoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DesenvolvedoraService {
    private final DesenvolvedoraRepository repository;

    public DesenvolvedoraService(DesenvolvedoraRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Desenvolvedora cadastrar(Desenvolvedora desenvolvedora) {
        if (repository.existsByCodigo(desenvolvedora.getCodigo())) {
            throw new RecursoDuplicadoException("Código da desenvolvedora já cadastrado");
        }

        return repository.save(desenvolvedora);
    }

    @Transactional(readOnly = true)
    public Desenvolvedora buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desenvolvedora não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<Desenvolvedora> listar() {
        return repository.findAll();
    }
}