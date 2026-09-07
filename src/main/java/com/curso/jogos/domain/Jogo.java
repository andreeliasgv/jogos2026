package com.curso.jogos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "jogo",
uniqueConstraints = @UniqueConstraint(
        name = "uk_jogo_codigo_jogo",
        columnNames = "codigo_jogo"))
public class Jogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_jogo", nullable = false, length = 50)
    private String codigoJogo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(name = "quantidade_disponivel", nullable = false)
    private int quantidadeDisponivel;

    @Column(name = "preco_unitario",
            nullable = false,
            precision = 18,
            scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "estoque_minimo", nullable = false)
    private int estoqueMinimo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "desenvolvedora_id",
            foreignKey = @ForeignKey(
                    name = "fk_jogo_desenvolvedora"))
    private Desenvolvedora desenvolvedora;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "genero_jogo_id",
                nullable = false,
                foreignKey = @ForeignKey(name = "fk_jogo_genero_jogo"))
    private GeneroJogo genero;

    protected Jogo(){}

    public Jogo(String codigoJogo, String titulo, int quantidadeDisponivel, BigDecimal precoUnitario, LocalDate dataCadastro) {
        this(codigoJogo, titulo, quantidadeDisponivel, precoUnitario, 0, dataCadastro);
    }

    public Jogo(String codigoJogo, String titulo, int quantidadeDisponivel, BigDecimal precoUnitario, int estoqueMinimo, LocalDate dataCadastro) {
        this.codigoJogo = validarTextoObrigatorio(codigoJogo, "Código do jogo é obrigatório");
        this.titulo = validarTextoObrigatorio(titulo, "Título é obrigatório");
        this.quantidadeDisponivel = validarQuantidadeNaoNegativa(quantidadeDisponivel, "Quantidade disponível não pode ser negativa");
        this.precoUnitario = validarValorNaoNegativo(precoUnitario, "Preço unitário não pode ser negativo");
        this.estoqueMinimo = validarQuantidadeNaoNegativa(estoqueMinimo, "Estoque mínimo não pode ser negativo");
        this.dataCadastro = Objects.requireNonNull(dataCadastro, "Data de cadastro é obrigatória");
        this.status = Status.ATIVO;
    }

    public BigDecimal calcularValorInventario() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidadeDisponivel)).setScale(2, RoundingMode.HALF_UP);
    }

    public void receberUnidades(int quantidade) {
        validarQuantidadePositiva(quantidade, "Quantidade recebida deve ser maior que zero");

        this.quantidadeDisponivel += quantidade;
    }

    public void retirarUnidades(int quantidade) {
        validarQuantidadePositiva(quantidade, "Quantidade retirada deve ser maior que zero");

        if (quantidadeDisponivel < quantidade) {
            throw new IllegalArgumentException("Quantidade disponível insuficiente");
        }

        this.quantidadeDisponivel -= quantidade;
    }

    public void alterarTitulo(String novoTitulo) {
        this.titulo = validarTextoObrigatorio(novoTitulo, "Título é obrigatório");
    }

    public void alterarPrecoUnitario(BigDecimal novoPreco) {
        this.precoUnitario = validarValorNaoNegativo(novoPreco, "Preço unitário não pode ser negativo");
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }

    void associarAo(GeneroJogo genero) {
        Objects.requireNonNull(genero, "Gênero do jogo é obrigatório");

        if (this.genero != null && this.genero != genero) {
            throw new IllegalStateException("Jogo já pertence a outro gênero");
        }

        this.genero = genero;
    }

    public void associarDesenvolvedora(Desenvolvedora desenvolvedora) {
        this.desenvolvedora = Objects.requireNonNull(desenvolvedora, "Desenvolvedora é obrigatória");
    }

    public Long getId() { return id; }

    public String getCodigoJogo() {
        return codigoJogo;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public int getEstoqueMinimo() { return estoqueMinimo; }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public Status getStatus() {
        return status;
    }

    public GeneroJogo getGenero() {
        return genero;
    }

    public Desenvolvedora getDesenvolvedora() { return desenvolvedora; }

    private static String validarTextoObrigatorio(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }

        return texto.trim();
    }

    private static int validarQuantidadeNaoNegativa(int quantidade, String mensagem) {
        if (quantidade < 0) {
            throw new IllegalArgumentException(mensagem);
        }

        return quantidade;
    }

    private static void validarQuantidadePositiva(int quantidade, String mensagem) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private static BigDecimal validarValorNaoNegativo(BigDecimal valor, String mensagem) {
        Objects.requireNonNull(valor, mensagem);

        if (valor.signum() < 0) {
            throw new IllegalArgumentException(mensagem);
        }

        return valor;
    }
}