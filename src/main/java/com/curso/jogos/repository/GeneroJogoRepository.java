package com.curso.jogos.repository;

import com.curso.jogos.domain.GeneroJogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GeneroJogoRepository extends
        JpaRepository<GeneroJogo, Long>,
        JpaSpecificationExecutor<GeneroJogo> {
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<GeneroJogo> findByNomeIgnoreCase(String nome);
}
