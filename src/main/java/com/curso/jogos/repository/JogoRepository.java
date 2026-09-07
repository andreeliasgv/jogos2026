package com.curso.jogos.repository;

import com.curso.jogos.domain.Jogo;
import com.curso.jogos.domain.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JogoRepository extends JpaRepository<Jogo, Long> {

    Optional<Jogo> findByCodigoJogo(String codigoJogo);

    boolean existsByCodigoJogo(String codigoJogo);

    List<Jogo> findByGeneroId(Long generoId);

    List<Jogo> findByStatus(Status status);

    @EntityGraph(attributePaths = {"genero", "desenvolvedora"})
    @Query("select j from Jogo j order by j.id")
    List<Jogo> buscarTodosComRelacionamentos();

    @EntityGraph(attributePaths = {"genero", "desenvolvedora"})
    @Query("select j from Jogo j where j.id = :id")
    Optional<Jogo> buscarPorIdComRelacionamentos(@Param("id") Long id);
}