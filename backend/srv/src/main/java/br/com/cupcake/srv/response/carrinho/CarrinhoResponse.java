package br.com.cupcake.srv.response.carrinho;

import br.com.cupcake.srv.model.CarrinhoModel;
import br.com.cupcake.srv.model.ItemCarrinhoModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CarrinhoResponse(
        UUID idCarrinho,
        String status,
        List<ItemCarrinhoResponse> itens,
        BigDecimal subtotal
) {
    public static CarrinhoResponse from(CarrinhoModel carrinho, List<ItemCarrinhoModel> itens) {
        List<ItemCarrinhoResponse> responses = itens.stream().map(ItemCarrinhoResponse::from).toList();
        BigDecimal subtotal = responses.stream()
                .map(ItemCarrinhoResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarrinhoResponse(carrinho.getId(), carrinho.getStatus().name(), responses, subtotal);
    }
}
