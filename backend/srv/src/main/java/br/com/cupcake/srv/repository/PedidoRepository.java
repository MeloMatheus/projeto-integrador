package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.PedidoModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<PedidoModel, UUID> {
    Page<PedidoModel> findByUsuarioId(UUID usuarioId, Pageable pageable);

    Optional<PedidoModel> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    long countByStatusPedidoNome(String nome);

    long countByDataPedidoGreaterThanEqualAndDataPedidoLessThan(LocalDateTime inicio, LocalDateTime fim);

    Page<PedidoModel> findByDataPedidoGreaterThanEqualAndDataPedidoLessThanOrderByDataPedidoDesc(
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    );

    @Query("""
            select p.statusPedido.nome as status, count(p) as quantidade
            from PedidoModel p
            where p.dataPedido >= :inicio and p.dataPedido < :fim
            group by p.statusPedido.nome
            order by p.statusPedido.nome
            """)
    List<PedidoStatusProjection> contarPorStatusNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoModel p where p.id = :id")
    Optional<PedidoModel> findByIdForUpdate(@Param("id") UUID id);

    interface PedidoStatusProjection {
        String getStatus();

        long getQuantidade();
    }
}
