package br.com.cupcake.srv.request.pedido;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CriarPedidoRequest(
        @NotNull UUID idEndereco,
        @Size(max = 50) String codigoCupom
) {
}
