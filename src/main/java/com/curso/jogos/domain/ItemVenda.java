package com.curso.jogos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.util.Objects;

@Entity
@Table(name = "item_venda", uniqueConstraints = @UniqueConstraint(
        name = "uk_item_venda_jogo", columnNames = {"venda_id", "jogo_id"}))
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_venda_venda"))
    private Venda venda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jogo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_venda_jogo"))
    private Jogo jogo;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 18, scale = 2)
    private BigDecimal precoUnitario;

    protected ItemVenda() {
    }

    ItemVenda(Venda venda, Jogo jogo, int quantidade, BigDecimal precoUnitario) {
        this.venda = Objects.requireNonNull(venda, "Venda é obrigatória");
        this.jogo = Objects.requireNonNull(jogo, "Jogo é obrigatório");
        this.quantidade = validarPositivo(quantidade);
        this.precoUnitario = validarNaoNegativo(precoUnitario);
    }

    public BigDecimal calcularSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public Venda getVenda() { return venda; }
    public Jogo getJogo() { return jogo; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }

    private static int validarPositivo(int valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        return valor;
    }

    private static BigDecimal validarNaoNegativo(BigDecimal valor) {
        Objects.requireNonNull(valor, "Preço unitário é obrigatório");
        if (valor.signum() < 0) {
            throw new IllegalArgumentException("Preço unitário não pode ser negativo");
        }
        return valor;
    }
}
