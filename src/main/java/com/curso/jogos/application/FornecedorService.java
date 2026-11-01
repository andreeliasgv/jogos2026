package com.curso.jogos.application;

import com.curso.jogos.domain.Fornecedor;
import com.curso.jogos.repository.FornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FornecedorService {
    private final FornecedorRepository repository;

    public FornecedorService(FornecedorRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Fornecedor cadastrar(Fornecedor fornecedor) {
        if (repository.existsByCnpj(fornecedor.getCnpj())) {
            throw new RecursoDuplicadoException("CNPJ já cadastrado");
        }
        return repository.save(fornecedor);
    }

    @Transactional(readOnly = true)
    public Fornecedor buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Fornecedor> listar() { return repository.findAll(); }
}
