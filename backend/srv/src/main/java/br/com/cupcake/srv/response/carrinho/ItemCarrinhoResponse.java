package br.com.cupcake.srv.response.carrinho;

import br.com.cupcake.srv.model.ItemCarrinhoModel;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemCarrinhoResponse(
        UUID idProduto,
        String nomeProduto,
        Long quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
    public static ItemCarrinhoResponse from(ItemCarrinhoModel item) {
        return new ItemCarrinhoResponse(
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()))
        );
    }
}
