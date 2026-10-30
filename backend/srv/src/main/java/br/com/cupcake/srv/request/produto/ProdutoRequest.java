package br.com.cupcake.srv.request.produto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank @Size(max = 120) String nome,
        String descricao,
        @Size(max = 500) String imagemUrl,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal preco,
        @NotNull Boolean disponivel
) {
}
