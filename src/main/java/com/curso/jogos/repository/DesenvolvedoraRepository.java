package com.curso.jogos.repository;

import com.curso.jogos.domain.Desenvolvedora;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesenvolvedoraRepository
        extends JpaRepository<Desenvolvedora, Long> {
    boolean existsByCodigo(String codigo);
}
