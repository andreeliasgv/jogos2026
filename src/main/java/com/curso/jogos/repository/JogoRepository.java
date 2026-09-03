package com.curso.jogos.repository;

import com.curso.jogos.domain.Jogo;
import com.curso.jogos.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JogoRepository extends JpaRepository<Jogo, Long> {
    Optional<Jogo> findByCodigoJogo(String codigoJogo);

    boolean existsByCodigoJogo(String codigoJogo);

    List<Jogo> findByGeneroId(Long generoId);

    List<Jogo> findByStatus(Status status);
}
