package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.UsoCupomModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsoCupomRepository extends JpaRepository<UsoCupomModel, UUID> {
    long countByCupomId(UUID cupomId);
}
