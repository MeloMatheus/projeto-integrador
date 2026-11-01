package br.com.cupcake.srv.request.pagamento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarPagamentoRequest(
        @NotBlank @Size(max = 40) String metodo,
        @NotBlank @Size(max = 100) String chaveIdempotencia
) {
}
