package com.curso.jogos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Locale;

@Entity
@Table(
        name = "desenvolvedora",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_desenvolvedora_codigo",
                columnNames = "codigo"))
public class Desenvolvedora {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    protected Desenvolvedora() {
    }

    public Desenvolvedora(String nome, String codigo) {
        this.nome = validarTextoObrigatorio(
                nome,
                "Nome da desenvolvedora é obrigatório");

        this.codigo = validarCodigo(codigo);
        this.status = Status.ATIVO;
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public Status getStatus() {
        return status;
    }

    private static String validarTextoObrigatorio(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }

        return texto.trim();
    }

    private static String validarCodigo(String codigo) {
        String valor = validarTextoObrigatorio(
                codigo,
                "Código da desenvolvedora é obrigatório");

        if (!valor.matches("[A-Za-z0-9-]{2,30}")) {
            throw new IllegalArgumentException(
                    "Código deve possuir entre 2 e 30 caracteres alfanuméricos ou hífen");
        }

        return valor.toUpperCase(Locale.ROOT);
    }
}
