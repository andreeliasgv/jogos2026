package com.curso.jogos.application;

import com.curso.jogos.domain.Cliente;
import com.curso.jogos.domain.Colaborador;
import com.curso.jogos.domain.FuncaoColaborador;
import com.curso.jogos.domain.GeneroJogo;
import com.curso.jogos.domain.Jogo;
import com.curso.jogos.domain.Pessoa;
import com.curso.jogos.domain.StatusVenda;
import com.curso.jogos.domain.Venda;
import com.curso.jogos.repository.ClienteRepository;
import com.curso.jogos.repository.ColaboradorRepository;
import com.curso.jogos.repository.GeneroJogoRepository;
import com.curso.jogos.repository.JogoRepository;
import com.curso.jogos.repository.PessoaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class VendaServiceTest {
    @Autowired VendaService vendaService;
    @Autowired PessoaRepository pessoaRepository;
    @Autowired ClienteRepository clienteRepository;
    @Autowired ColaboradorRepository colaboradorRepository;
    @Autowired GeneroJogoRepository generoRepository;
    @Autowired JogoRepository jogoRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @AfterEach
    void limparDadosDoCenario() {
        jdbcTemplate.update("DELETE FROM item_venda");
        jdbcTemplate.update("DELETE FROM venda");
        jdbcTemplate.update("DELETE FROM cliente");
        jdbcTemplate.update("DELETE FROM colaborador");
        jdbcTemplate.update("DELETE FROM pessoa");
        jdbcTemplate.update("DELETE FROM jogo WHERE codigo_jogo LIKE 'VENDA-%'");
        jdbcTemplate.update("DELETE FROM genero_jogo WHERE nome LIKE 'Gênero VENDA-%'");
    }

    @Test
    void deveFinalizarVendaEBaixarEstoque() {
        Participantes participantes = criarParticipantes();
        Jogo jogo = criarJogo("VENDA-OK-" + System.nanoTime(), 5);
        Venda venda = vendaService.cadastrar(participantes.cliente().getId(),
                participantes.vendedor().getId());
        vendaService.adicionarItem(venda.getId(), jogo.getId(), 2);

        Venda finalizada = vendaService.finalizar(venda.getId());

        assertEquals(StatusVenda.FINALIZADA, finalizada.getStatus());
        assertEquals(3, jogoRepository.findById(jogo.getId()).orElseThrow().getQuantidadeDisponivel());
    }

    @Test
    void deveDesfazerTodasAsBaixasQuandoUmItemNaoTemEstoque() {
        Participantes participantes = criarParticipantes();
        Jogo primeiro = criarJogo("VENDA-ROLLBACK-A-" + System.nanoTime(), 5);
        Jogo segundo = criarJogo("VENDA-ROLLBACK-B-" + System.nanoTime(), 1);
        Venda venda = vendaService.cadastrar(participantes.cliente().getId(),
                participantes.vendedor().getId());
        vendaService.adicionarItem(venda.getId(), primeiro.getId(), 2);
        vendaService.adicionarItem(venda.getId(), segundo.getId(), 2);

        assertThrows(IllegalArgumentException.class, () -> vendaService.finalizar(venda.getId()));

        assertEquals(5, jogoRepository.findById(primeiro.getId()).orElseThrow().getQuantidadeDisponivel());
        assertEquals(StatusVenda.ABERTA, vendaService.buscarPorId(venda.getId()).getStatus());
    }

    @Test
    void deveImpedirVendaAcimaDoEstoqueEmFinalizacoesConcorrentes()
            throws Exception {
        Participantes participantes = criarParticipantes();
        Jogo jogo = criarJogo("VENDA-CONCORRENTE-" + System.nanoTime(), 5);
        Venda primeira = criarVendaComItem(participantes, jogo, 4);
        Venda segunda = criarVendaComItem(participantes, jogo, 4);

        CountDownLatch inicioSimultaneo = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<ResultadoFinalizacao> primeiroResultado = executor.submit(
                    () -> finalizarAposSinal(primeira.getId(), inicioSimultaneo));
            Future<ResultadoFinalizacao> segundoResultado = executor.submit(
                    () -> finalizarAposSinal(segunda.getId(), inicioSimultaneo));

            inicioSimultaneo.countDown();

            List<ResultadoFinalizacao> resultados = List.of(
                    primeiroResultado.get(10, TimeUnit.SECONDS),
                    segundoResultado.get(10, TimeUnit.SECONDS));

            assertEquals(1, resultados.stream()
                    .filter(ResultadoFinalizacao::sucesso)
                    .count());
            assertEquals(1, resultados.stream()
                    .filter(resultado -> !resultado.sucesso())
                    .count());
            assertTrue(resultados.stream()
                    .filter(resultado -> !resultado.sucesso())
                    .allMatch(resultado -> "Quantidade disponível insuficiente"
                            .equals(resultado.mensagem())));
        }

        assertEquals(1, jogoRepository.findById(jogo.getId())
                .orElseThrow()
                .getQuantidadeDisponivel());

        List<StatusVenda> estados = List.of(
                vendaService.buscarPorId(primeira.getId()).getStatus(),
                vendaService.buscarPorId(segunda.getId()).getStatus());
        assertEquals(1, estados.stream()
                .filter(StatusVenda.FINALIZADA::equals)
                .count());
        assertEquals(1, estados.stream()
                .filter(StatusVenda.ABERTA::equals)
                .count());
    }

    private Venda criarVendaComItem(
            Participantes participantes,
            Jogo jogo,
            int quantidade) {
        Venda venda = vendaService.cadastrar(
                participantes.cliente().getId(),
                participantes.vendedor().getId());
        vendaService.adicionarItem(venda.getId(), jogo.getId(), quantidade);
        return venda;
    }

    private ResultadoFinalizacao finalizarAposSinal(
            Long vendaId,
            CountDownLatch inicioSimultaneo)
            throws InterruptedException {
        inicioSimultaneo.await();
        try {
            vendaService.finalizar(vendaId);
            return new ResultadoFinalizacao(true, null);
        } catch (IllegalArgumentException exception) {
            return new ResultadoFinalizacao(false, exception.getMessage());
        }
    }

    private Participantes criarParticipantes() {
        long semente = System.nanoTime();
        String sufixo = String.valueOf(semente);
        Pessoa pessoaCliente = pessoaRepository.save(new Pessoa("Cliente", "cliente" + sufixo + "@example.com",
                gerarCpf(semente), LocalDate.now()));
        Pessoa pessoaVendedor = pessoaRepository.save(new Pessoa("Vendedor", "vendedor" + sufixo + "@example.com",
                gerarCpf(semente + 7919), LocalDate.now()));
        Cliente cliente = clienteRepository.save(new Cliente(pessoaCliente, null));
        Colaborador vendedor = colaboradorRepository.save(new Colaborador(pessoaVendedor, "M-" + sufixo,
                FuncaoColaborador.VENDEDOR, LocalDate.now()));
        return new Participantes(cliente, vendedor);
    }

    private Jogo criarJogo(String codigo, int quantidade) {
        GeneroJogo genero = generoRepository.save(new GeneroJogo("Gênero " + codigo));
        Jogo jogo = new Jogo(codigo, "Jogo de teste", quantidade,
                new BigDecimal("10.00"), LocalDate.now());
        genero.adicionarJogo(jogo);
        return jogoRepository.save(jogo);
    }

    private String gerarCpf(long semente) {
        String base = "%09d".formatted(Math.floorMod(semente, 1_000_000_000L));
        if (digitosRepetidos(base)) base = "123456789";
        int primeiro = digitoCpf(base, 10);
        return base + primeiro + digitoCpf(base + primeiro, 11);
    }

    private int digitoCpf(String base, int peso) {
        int soma = 0;
        for (char caractere : base.toCharArray()) {
            soma += Character.getNumericValue(caractere) * peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private boolean digitosRepetidos(String valor) {
        for (int indice = 1; indice < valor.length(); indice++) {
            if (valor.charAt(indice) != valor.charAt(0)) return false;
        }
        return true;
    }

    private record Participantes(Cliente cliente, Colaborador vendedor) { }

    private record ResultadoFinalizacao(boolean sucesso, String mensagem) { }
}
