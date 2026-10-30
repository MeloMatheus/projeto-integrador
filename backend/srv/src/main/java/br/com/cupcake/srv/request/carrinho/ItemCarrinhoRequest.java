package br.com.cupcake.srv.request.carrinho;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ItemCarrinhoRequest(
        @NotNull UUID idProduto,
        @NotNull @Positive @Max(1000) Integer quantidade
) {
}
