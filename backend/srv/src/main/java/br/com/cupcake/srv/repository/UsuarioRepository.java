package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.UsuarioModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, UUID> {
    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    Optional<UsuarioModel> findByEmailIgnoreCaseAndAtivoTrue(String email);

    Optional<UsuarioModel> findByEmailIgnoreCase(String email);

    Optional<UsuarioModel> findByIdAndAtivoTrue(UUID id);

    Page<UsuarioModel> findByAtivoTrue(Pageable pageable);

    long countByAtivoTrue();
}
