package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.PagamentoModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository extends JpaRepository<PagamentoModel, UUID> {
    List<PagamentoModel> findByPedidoId(UUID pedidoId);

    Optional<PagamentoModel> findByTransacaoExterna(String transacaoExterna);

    Optional<PagamentoModel> findByChaveIdempotencia(String chaveIdempotencia);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PagamentoModel p where p.id = :id")
    Optional<PagamentoModel> findByIdForUpdate(@Param("id") UUID id);

    @Query("select coalesce(sum(p.valor), 0) from PagamentoModel p where p.statusPagamento.nome = 'APROVADO'")
    java.math.BigDecimal totalAprovado();
}
