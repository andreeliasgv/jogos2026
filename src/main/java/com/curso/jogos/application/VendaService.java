package com.curso.jogos.application;

import com.curso.jogos.domain.Cliente;
import com.curso.jogos.domain.Colaborador;
import com.curso.jogos.domain.FuncaoColaborador;
import com.curso.jogos.domain.ItemVenda;
import com.curso.jogos.domain.Jogo;
import com.curso.jogos.domain.Status;
import com.curso.jogos.domain.StatusVenda;
import com.curso.jogos.domain.Venda;
import com.curso.jogos.repository.ClienteRepository;
import com.curso.jogos.repository.ColaboradorRepository;
import com.curso.jogos.repository.JogoRepository;
import com.curso.jogos.repository.VendaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class VendaService {
    private final VendaRepository repository;
    private final ClienteRepository clienteRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final JogoRepository jogoRepository;

    public VendaService(VendaRepository repository, ClienteRepository clienteRepository,
                        ColaboradorRepository colaboradorRepository, JogoRepository jogoRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.jogoRepository = jogoRepository;
    }

    @Transactional
    public Venda cadastrar(Long clienteId, Long vendedorId) {
        Cliente cliente = buscarCliente(clienteId);
        Colaborador vendedor = buscarVendedor(vendedorId);
        validarParticipantes(cliente, vendedor);
        return repository.save(new Venda(cliente, vendedor, LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public Venda buscarPorId(Long id) { return buscarEntidade(id); }

    @Transactional
    public Venda adicionarItem(Long vendaId, Long jogoId, int quantidade) {
        Venda venda = buscarEntidade(vendaId);
        Jogo jogo = jogoRepository.buscarPorIdComRelacionamentos(jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        if (jogo.getStatus() != Status.ATIVO) throw new IllegalArgumentException("Jogo deve estar ativo");
        venda.adicionarItem(jogo, quantidade);
        return venda;
    }

    @Transactional
    public Venda removerItem(Long vendaId, Long itemId) {
        Venda venda = buscarEntidade(vendaId);
        venda.removerItem(itemId);
        return venda;
    }

    @Transactional
    public Venda finalizar(Long id) {
        Venda venda = buscarEntidade(id);
        validarParticipantes(venda.getCliente(), venda.getVendedor());
        for (ItemVenda item : venda.getItens()) {
            if (item.getJogo().getStatus() != Status.ATIVO) {
                throw new IllegalArgumentException("Todos os jogos da venda devem estar ativos");
            }
            item.getJogo().retirarUnidades(item.getQuantidade());
        }
        venda.finalizar();
        return venda;
    }

    @Transactional
    public Venda cancelar(Long id) {
        Venda venda = buscarEntidade(id);
        venda.cancelar();
        return venda;
    }

    @Transactional(readOnly = true)
    public Page<Venda> pesquisar(StatusVenda status, Long clienteId, Long vendedorId,
                                 LocalDate dataInicial, LocalDate dataFinal, Pageable pageable) {
        validarOrdenacao(pageable, Set.of("id", "dataVenda", "status"));
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }
        Specification<Venda> filtros = (root, query, cb) -> cb.conjunction();
        if (status != null) filtros = filtros.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (clienteId != null) filtros = filtros.and((root, query, cb) ->
                cb.equal(root.get("cliente").get("id"), clienteId));
        if (vendedorId != null) filtros = filtros.and((root, query, cb) ->
                cb.equal(root.get("vendedor").get("id"), vendedorId));
        if (dataInicial != null) filtros = filtros.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("dataVenda"), dataInicial.atStartOfDay()));
        if (dataFinal != null) filtros = filtros.and((root, query, cb) ->
                cb.lessThan(root.get("dataVenda"), dataFinal.plusDays(1).atStartOfDay()));
        return repository.findAll(filtros, pageable);
    }

    private void validarParticipantes(Cliente cliente, Colaborador vendedor) {
        if (cliente.getPessoa().getStatus() != Status.ATIVO) {
            throw new IllegalArgumentException("Cliente deve estar ativo");
        }
        if (vendedor.getPessoa().getStatus() != Status.ATIVO) {
            throw new IllegalArgumentException("Vendedor deve estar ativo");
        }
        if (vendedor.getFuncao() != FuncaoColaborador.VENDEDOR
                && vendedor.getFuncao() != FuncaoColaborador.GERENTE) {
            throw new IllegalArgumentException("Colaborador não possui função habilitada para vender");
        }
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    private Colaborador buscarVendedor(Long id) {
        return colaboradorRepository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado"));
    }

    private Venda buscarEntidade(Long id) {
        return repository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda não encontrada"));
    }

    private void validarOrdenacao(Pageable pageable, Set<String> permitidos) {
        pageable.getSort().forEach(ordem -> {
            if (!permitidos.contains(ordem.getProperty())) {
                throw new IllegalArgumentException("Campo de ordenação inválido: " + ordem.getProperty());
            }
        });
    }
}
