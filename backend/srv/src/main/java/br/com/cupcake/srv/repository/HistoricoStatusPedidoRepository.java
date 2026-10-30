package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.HistoricoStatusPedidoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoStatusPedidoRepository extends JpaRepository<HistoricoStatusPedidoModel, UUID> {
    List<HistoricoStatusPedidoModel> findByPedidoIdOrderByDataAlteracaoAsc(UUID pedidoId);
}
