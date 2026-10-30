package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.PerfilModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerfilRepository extends JpaRepository<PerfilModel, UUID> {
    Optional<PerfilModel> findByNomeIgnoreCase(String nome);
}
