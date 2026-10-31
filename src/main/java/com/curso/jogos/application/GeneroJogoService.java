package com.curso.jogos.application;

import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.domain.Status;
import com.curso.jogos.repository.GeneroJogoRepository;
import com.curso.jogos.repository.JogoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class GeneroJogoService {
    private final GeneroJogoRepository repository;
    private final JogoRepository jogoRepository;

    public GeneroJogoService(
            GeneroJogoRepository repository,
            JogoRepository jogoRepository) {
        this.repository = repository;
        this.jogoRepository = jogoRepository;
    }


    @Transactional
    public GeneroJogo cadastrar(String nome) {

        String nomeNormalizado = normalizarNome(nome);

        if (repository.existsByNomeIgnoreCase(nomeNormalizado)) {
            throw new RecursoDuplicadoException("Nome do gênero já cadastrado");
        }

        return repository.save(new GeneroJogo(nomeNormalizado));
    }

    @Transactional
    public GeneroJogo alterar(Long id, String nome) {
        String nomeNormalizado = normalizarNome(nome);

        if (repository.existsByNomeIgnoreCaseAndIdNot(nomeNormalizado, id)) {
            throw new RecursoDuplicadoException("Nome do gênero já cadastrado");
        }

        GeneroJogo genero = buscarEntidade(id);
        genero.alterarNome(nomeNormalizado);
        return genero;
    }

    @Transactional
    public GeneroJogo alterarStatus(long id, Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }

        GeneroJogo genero = buscarEntidade(id);

        if (status == Status.ATIVO) {
            genero.ativar();
        } else {
            genero.inativar();
        }

        return genero;
    }

    @Transactional
    public void excluir(long id) {
        GeneroJogo genero = buscarEntidade(id);

        if (jogoRepository.existsByGeneroId(id)) {
            throw new RecursoEmUsoException("Gênero não pode ser excluído porque possui jogos");
        }

        repository.delete(genero);
        repository.flush();
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

    @Transactional(readOnly = true)
    public Page<GeneroJogo> pesquisar(String nome, Status status, Pageable pageable) {
        validarOrdenacao(pageable, Set.of("id", "nome", "status"));

        Specification<GeneroJogo> filtros = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (nome != null && !nome.isBlank()) {
            String trecho = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";

            filtros = filtros.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), trecho)));
        }

        if (status != null) {
            filtros = filtros.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status)));
        }

        return repository.findAll(filtros, pageable);
    }

    private GeneroJogo buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Gênero de jogo não encontrado"));
    }

    private String normalizarNome(String nome) {
        return nome == null ? null : nome.trim();
    }

    private void validarOrdenacao(Pageable pageable, Set<String> camposPermitidos) {
        pageable.getSort().forEach(ordem -> {
            if (!camposPermitidos.contains(ordem.getProperty())) {
                throw new IllegalArgumentException("Campo de ordenação inválido: " + ordem.getProperty());
            }
        });
    }
}