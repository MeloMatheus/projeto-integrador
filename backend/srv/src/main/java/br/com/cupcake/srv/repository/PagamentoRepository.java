package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.PagamentoModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Query("""
            select coalesce(sum(p.valor), 0)
            from PagamentoModel p
            where p.statusPagamento.nome = 'APROVADO'
              and p.dataConfirmacao >= :inicio and p.dataConfirmacao < :fim
            """)
    BigDecimal totalAprovadoNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("""
            select count(distinct p.pedido.id)
            from PagamentoModel p
            where p.statusPagamento.nome = 'APROVADO'
              and p.dataConfirmacao >= :inicio and p.dataConfirmacao < :fim
            """)
    long contarPedidosComPagamentoAprovadoNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("""
            select p.dataConfirmacao as dataConfirmacao, p.valor as valor
            from PagamentoModel p
            where p.statusPagamento.nome = 'APROVADO'
              and p.dataConfirmacao >= :inicio and p.dataConfirmacao < :fim
            """)
    List<FaturamentoConfirmadoProjection> buscarFaturamentosAprovadosNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    interface FaturamentoConfirmadoProjection {
        LocalDateTime getDataConfirmacao();

        BigDecimal getValor();
    }
}
