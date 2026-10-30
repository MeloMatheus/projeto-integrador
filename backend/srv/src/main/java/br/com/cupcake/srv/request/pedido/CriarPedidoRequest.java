package br.com.cupcake.srv.request.pedido;

import jakarta.validation.constraints.Size;

public record CriarPedidoRequest(@Size(max = 50) String codigoCupom) {
}
