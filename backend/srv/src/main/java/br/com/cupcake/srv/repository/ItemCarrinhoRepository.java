package br.com.cupcake.srv.repository;

import br.com.cupcake.srv.model.ItemCarrinhoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemCarrinhoRepository extends JpaRepository<ItemCarrinhoModel, UUID> {
    List<ItemCarrinhoModel> findByCarrinhoId(UUID carrinhoId);

    Optional<ItemCarrinhoModel> findByCarrinhoIdAndProdutoId(UUID carrinhoId, UUID produtoId);

    void deleteByCarrinhoId(UUID carrinhoId);
}
