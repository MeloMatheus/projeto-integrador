package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.EnderecoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnderecoRepository extends JpaRepository<EnderecoModel, UUID> {
    List<EnderecoModel> findByUsuarioIdOrderByPrincipalDescCriadoEmDesc(UUID usuarioId);

    Optional<EnderecoModel> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
