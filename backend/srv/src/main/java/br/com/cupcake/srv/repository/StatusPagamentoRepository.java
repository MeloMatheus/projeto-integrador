package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.StatusPagamentoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusPagamentoRepository extends JpaRepository<StatusPagamentoModel, Integer> {
    Optional<StatusPagamentoModel> findByNome(String nome);
}
