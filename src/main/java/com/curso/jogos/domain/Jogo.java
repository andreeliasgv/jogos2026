package com.curso.jogos.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public class Jogo {
    private final String codigoJogo;
    private String titulo;
    private int quantidadeDisponivel;
    private BigDecimal precoUnitario;
    private final LocalDate dataCadastro;
    private Status status;
    private GeneroJogo genero;

    public Jogo(String codigoJogo, String titulo, int quantidadeDisponivel, BigDecimal precoUnitario, LocalDate dataCadastro) {
        this.codigoJogo = validarTextoObrigatorio(codigoJogo, "Código do jogo é obrigatório");
        this.titulo = validarTextoObrigatorio(titulo, "Título é obrigatório");
        this.quantidadeDisponivel = validarQuantidadeNaoNegativa(quantidadeDisponivel, "Quantidade disponível não pode ser negativa");
        this.precoUnitario = validarValorNaoNegativo(precoUnitario, "Preço unitário não pode ser negativo");
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

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public Status getStatus() {
        return status;
    }

    public GeneroJogo getGenero() {
        return genero;
    }

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