package com.curso.jogos.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "conta_acesso")
public class ContaAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
    private Pessoa pessoa;

    @Column(nullable = false, length = 80)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusContaAcesso status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "conta_acesso_perfil",
            joinColumns = @JoinColumn(name = "conta_acesso_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, length = 30)
    private Set<PerfilAcesso> perfis = new LinkedHashSet<>();

    protected ContaAcesso() {
    }

    public ContaAcesso(Pessoa pessoa, String login, String senhaHash) {
        this.pessoa = Objects.requireNonNull(pessoa, "Pessoa é obrigatória");
        this.login = normalizarLogin(login);
        this.senhaHash = validarSenhaHash(senhaHash);
        this.status = StatusContaAcesso.ATIVA;
        this.perfis.add(PerfilAcesso.OPERADOR);
    }

    public void alterarSenha(String novaSenhaHash) {
        this.senhaHash = validarSenhaHash(novaSenhaHash);
    }

    public void bloquear() {
        this.status = StatusContaAcesso.BLOQUEADA;
    }

    public void ativar() {
        this.status = StatusContaAcesso.ATIVA;
    }

    public void concederPerfil(PerfilAcesso perfil) {
        this.perfis.add(Objects.requireNonNull(perfil, "Perfil é obrigatório"));
    }

    public boolean podeAutenticar() {
        return status == StatusContaAcesso.ATIVA
                && pessoa.getStatus() == Status.ATIVO;
    }

    public Long getId() { return id; }
    public Pessoa getPessoa() { return pessoa; }
    public String getLogin() { return login; }
    public String getSenhaHash() { return senhaHash; }
    public StatusContaAcesso getStatus() { return status; }
    public Set<PerfilAcesso> getPerfis() { return Collections.unmodifiableSet(perfis); }

    private static String normalizarLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login é obrigatório");
        }
        String valor = login.trim().toLowerCase(Locale.ROOT);
        if (!valor.matches("^[a-z0-9._-]{3,80}$")) {
            throw new IllegalArgumentException("Login inválido");
        }
        return valor;
    }

    private static String validarSenhaHash(String senhaHash) {
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("Hash da senha é obrigatório");
        }
        return senhaHash;
    }
}
