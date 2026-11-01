package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.StatusPagamentoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StatusPagamentoRepository extends JpaRepository<StatusPagamentoModel, UUID> {
    Optional<StatusPagamentoModel> findByNome(String nome);
}
