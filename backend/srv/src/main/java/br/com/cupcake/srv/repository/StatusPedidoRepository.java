package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.StatusPedidoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusPedidoRepository extends JpaRepository<StatusPedidoModel, Integer> {
    Optional<StatusPedidoModel> findByNome(String nome);
}
