package br.com.cupcake.srv.request.pedido;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarStatusPedidoRequest(@NotBlank String status, @Size(max = 500) String observacao) {
}
