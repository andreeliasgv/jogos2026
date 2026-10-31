package com.curso.jogos.repository;

import com.curso.jogos.domain.Jogo;
import com.curso.jogos.domain.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;


public interface JogoRepository extends
        JpaRepository<Jogo, Long>,
        JpaSpecificationExecutor<Jogo> {

    Optional<Jogo> findByCodigoJogo(String codigoJogo);

    boolean existsByCodigoJogo(String codigoJogo);
    boolean existsByGeneroId(Long generoId);

    List<Jogo> findByGeneroId(Long generoId);
    List<Jogo> findByStatus(Status status);

    @EntityGraph(attributePaths = {"genero", "desenvolvedora"})
    @Query("select j from Jogo j order by j.id")
    List<Jogo> buscarTodosComRelacionamentos();

    @EntityGraph(attributePaths = {"genero", "desenvolvedora"})
    @Query("select j from Jogo j where j.id = :id")
    Optional<Jogo> buscarPorIdComRelacionamentos(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"genero", "desenvolvedora"})
    Page<Jogo> findAll(Specification<Jogo> specification, Pageable pageable);
}