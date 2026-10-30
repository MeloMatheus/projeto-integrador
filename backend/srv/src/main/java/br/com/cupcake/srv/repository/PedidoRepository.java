package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.PedidoModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<PedidoModel, UUID> {
    Page<PedidoModel> findByUsuarioId(UUID usuarioId, Pageable pageable);

    Optional<PedidoModel> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    long countByStatusPedidoNome(String nome);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoModel p where p.id = :id")
    Optional<PedidoModel> findByIdForUpdate(@Param("id") UUID id);
}
