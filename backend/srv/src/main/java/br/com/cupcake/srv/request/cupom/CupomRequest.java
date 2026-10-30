package br.com.cupcake.srv.request.cupom;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CupomRequest(
        @NotBlank @Size(max = 50) String codigo,
        @Size(max = 255) String descricao,
        @NotBlank String tipoDesconto,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 8, fraction = 2) BigDecimal valorDesconto,
        @NotNull LocalDateTime dataInicio,
        @NotNull LocalDateTime dataFim,
        @Positive Integer limiteUtilizacoes,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal valorMinimoPedido,
        @NotNull Boolean ativo
) {
}
