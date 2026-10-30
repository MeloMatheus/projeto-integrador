package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.CupomModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CupomRepository extends JpaRepository<CupomModel, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CupomModel c where upper(c.codigo) = upper(:codigo)")
    Optional<CupomModel> findByCodigoForUpdate(@Param("codigo") String codigo);

    long countByAtivoTrue();
}
