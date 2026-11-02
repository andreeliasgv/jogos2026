package com.curso.jogos.application;

import com.curso.jogos.domain.ContaAcesso;
import com.curso.jogos.domain.Pessoa;
import com.curso.jogos.domain.Status;
import com.curso.jogos.repository.ContaAcessoRepository;
import com.curso.jogos.repository.PessoaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaAcessoService {
    private final ContaAcessoRepository repository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaAcessoService(
            ContaAcessoRepository repository,
            PessoaRepository pessoaRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ContaAcesso cadastrar(Long pessoaId, String login, String senhaAberta) {
        String loginNormalizado = validarLogin(login);
        validarSenhaAberta(senhaAberta, "Senha");

        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada"));

        if (pessoa.getStatus() != Status.ATIVO) {
            throw new IllegalArgumentException("Pessoa deve estar ativa");
        }
        if (repository.existsByPessoaId(pessoaId)) {
            throw new RecursoDuplicadoException("Pessoa já possui conta de acesso");
        }
        if (repository.existsByLoginIgnoreCase(loginNormalizado)) {
            throw new RecursoDuplicadoException("Login já cadastrado");
        }

        String senhaHash = passwordEncoder.encode(senhaAberta);
        return repository.save(new ContaAcesso(pessoa, loginNormalizado, senhaHash));
    }

    @Transactional(readOnly = true)
    public ContaAcesso buscarPorLogin(String login) {
        return buscarEntidade(login);
    }

    @Transactional
    public void alterarSenha(String login, String senhaAtual, String novaSenha) {
        validarSenhaAberta(senhaAtual, "Senha atual");
        validarSenhaAberta(novaSenha, "Nova senha");
        ContaAcesso conta = buscarEntidade(login);

        if (!passwordEncoder.matches(senhaAtual, conta.getSenhaHash())) {
            throw new IllegalArgumentException("Senha atual inválida");
        }
        if (passwordEncoder.matches(novaSenha, conta.getSenhaHash())) {
            throw new IllegalArgumentException("Nova senha deve ser diferente");
        }

        conta.alterarSenha(passwordEncoder.encode(novaSenha));
    }

    private ContaAcesso buscarEntidade(String login) {
        return repository.findByLoginIgnoreCase(login)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta de acesso não encontrada"));
    }

    private String validarLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login é obrigatório");
        }
        return login.trim();
    }

    private void validarSenhaAberta(String senha, String campo) {
        if (senha == null || senha.length() < 12 || senha.length() > 72) {
            throw new IllegalArgumentException(campo + " deve possuir entre 12 e 72 caracteres");
        }
    }
}
