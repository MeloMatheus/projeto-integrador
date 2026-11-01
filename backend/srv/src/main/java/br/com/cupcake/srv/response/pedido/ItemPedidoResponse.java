package br.com.cupcake.srv.response.pedido;

import br.com.cupcake.srv.model.ItemPedidoModel;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPedidoResponse(
        UUID idProduto,
        String nomeProduto,
        Long quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
    public static ItemPedidoResponse from(ItemPedidoModel item) {
        return new ItemPedidoResponse(
                item.getProduto().getId(),
                item.getNomeProdutoSnapshot(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()))
        );
    }
}
