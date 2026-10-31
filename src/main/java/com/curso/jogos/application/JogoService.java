package com.curso.jogos.application;

import com.curso.jogos.domain.Desenvolvedora;
import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.domain.Jogo;
import com.curso.jogos.domain.Status;
import com.curso.jogos.repository.DesenvolvedoraRepository;
import com.curso.jogos.repository.GeneroJogoRepository;
import com.curso.jogos.repository.JogoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.math.BigDecimal;

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

    @Transactional
    public Jogo alterar(Long id, String titulo, BigDecimal precoUnitario, int estoqueMinimo, Long generoId, Long desenvolvedoraId) {
        Jogo jogo = buscarEntidade(id);
        GeneroJogo genero = buscarGenero(generoId);

        Desenvolvedora desenvolvedora = desenvolvedoraId == null ? null : buscarDesenvolvedora(desenvolvedoraId);

        jogo.alterarTitulo(titulo);
        jogo.alterarPrecoUnitario(precoUnitario);
        jogo.alterarEstoqueMinimo(estoqueMinimo);
        jogo.alterarGenero(genero);
        jogo.alterarDesenvolvedora(desenvolvedora);

        return jogo;
    }

    @Transactional
    public Jogo alterarStatus(Long id, Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }

        Jogo jogo = buscarEntidade(id);

        if (status == Status.ATIVO) {
            jogo.ativar();
        } else {
            jogo.inativar();
        }

        return jogo;
    }

    @Transactional
    public void excluir(Long id) {
        Jogo jogo = buscarEntidade(id);
        jogoRepository.delete(jogo);
        jogoRepository.flush();
    }

    @Transactional(readOnly = true)
    public Page<Jogo> pesquisar(String titulo, Status status, Long generoId, Long desenvolvedoraId,
                                Boolean abaixoEstoqueMinimo, Pageable pageable) {
        validarOrdenacao(pageable, Set.of("id", "codigoJogo", "titulo", "quantidadeDisponivel",
                "precoUnitario", "estoqueMinimo", "dataCadastro", "status")
        );

        Specification<Jogo> filtros = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (titulo != null && !titulo.isBlank()) {
            String trecho = "%" + titulo.trim().toLowerCase(Locale.ROOT) + "%";

            filtros = filtros.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("titulo")), trecho));
        }

        if (status != null) {
            filtros = filtros.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }

        if (generoId != null) {
            filtros = filtros.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("genero").get("id"), generoId));
        }

        if (desenvolvedoraId != null) {
            filtros = filtros.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("desenvolvedora").get("id"), desenvolvedoraId));
        }

        if (Boolean.TRUE.equals(abaixoEstoqueMinimo)) {
            filtros = filtros.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("quantidadeDisponivel"), root.get("estoqueMinimo")));
        }

        return jogoRepository.findAll(filtros, pageable);
    }

    @Transactional(readOnly = true)
    public Jogo buscarPorId(Long id) {
        return buscarEntidade(id);
    }

    @Transactional(readOnly = true)
    public List<Jogo> listar() {
        return jogoRepository.buscarTodosComRelacionamentos();
    }

    @Transactional
    public Jogo receberUnidades(Long id, int quantidade) {
        Jogo jogo = buscarEntidade(id);
        jogo.receberUnidades(quantidade);
        return jogo;
    }

    @Transactional
    public Jogo retirarUnidades(Long id, int quantidade) {
        Jogo jogo = buscarEntidade(id);
        jogo.retirarUnidades(quantidade);
        return jogo;
    }

    private Jogo buscarEntidade(Long id) {
        return jogoRepository.buscarPorIdComRelacionamentos(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Jogo não encontrado"));
    }

    private GeneroJogo buscarGenero(Long id) {
        return generoRepository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Gênero de jogo não encontrado"));
    }

    private Desenvolvedora buscarDesenvolvedora(Long id) {
        return desenvolvedoraRepository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Desenvolvedora não encontrada"));
    }

    private void validarOrdenacao(Pageable pageable, Set<String> camposPermitidos) {
        pageable.getSort().forEach(ordem -> {
            if (!camposPermitidos.contains(ordem.getProperty())) {
                throw new IllegalArgumentException("Campo de ordenação inválido: " + ordem.getProperty());
            }
        });
    }
}