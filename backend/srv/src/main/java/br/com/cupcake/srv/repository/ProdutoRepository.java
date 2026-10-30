package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.ProdutoModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<ProdutoModel, UUID> {
    Page<ProdutoModel> findByAtivoTrueAndDisponivelTrue(Pageable pageable);

    Page<ProdutoModel> findByAtivoTrueAndDisponivelTrueAndNomeContainingIgnoreCase(String nome, Pageable pageable);

    Optional<ProdutoModel> findByIdAndAtivoTrue(UUID id);

    long countByAtivoTrue();
}
