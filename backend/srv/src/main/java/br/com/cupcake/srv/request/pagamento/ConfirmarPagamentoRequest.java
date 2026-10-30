package br.com.cupcake.srv.request.pagamento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmarPagamentoRequest(
        @NotBlank String resultado,
        @Size(max = 150) String transacaoExterna
) {
}
