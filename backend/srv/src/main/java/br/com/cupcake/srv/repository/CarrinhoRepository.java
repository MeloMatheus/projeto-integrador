package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.CarrinhoModel;
import br.com.cupcake.srv.model.StatusCarrinho;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CarrinhoRepository extends JpaRepository<CarrinhoModel, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CarrinhoModel c where c.usuario.id = :usuarioId and c.status = :status order by c.criadoEm desc")
    java.util.List<CarrinhoModel> findOpenByUsuarioIdForUpdate(
            @Param("usuarioId") UUID usuarioId,
            @Param("status") StatusCarrinho status
    );

    Optional<CarrinhoModel> findFirstByUsuarioIdAndStatusOrderByCriadoEmDesc(UUID usuarioId, StatusCarrinho status);
}
