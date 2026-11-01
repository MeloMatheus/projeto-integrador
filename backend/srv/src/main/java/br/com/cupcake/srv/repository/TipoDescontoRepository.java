package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.TipoDescontoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TipoDescontoRepository extends JpaRepository<TipoDescontoModel, UUID> {
    Optional<TipoDescontoModel> findByNome(String nome);
}
