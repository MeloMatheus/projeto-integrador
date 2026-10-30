package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.ItemPedidoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemPedidoRepository extends JpaRepository<ItemPedidoModel, UUID> {
    List<ItemPedidoModel> findByPedidoId(UUID pedidoId);
}
