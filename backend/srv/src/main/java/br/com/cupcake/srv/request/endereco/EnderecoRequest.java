package br.com.cupcake.srv.request.endereco;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(
        @Size(max = 50) String identificacao,
        @NotBlank @Size(max = 120) String destinatario,
        @NotBlank @Size(max = 10) String cep,
        @NotBlank @Size(max = 150) String logradouro,
        @NotBlank @Size(max = 20) String numero,
        @Size(max = 100) String complemento,
        @NotBlank @Size(max = 100) String bairro,
        @NotBlank @Size(max = 100) String cidade,
        @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String uf,
        @Size(max = 200) String referencia,
        @NotNull Boolean principal
) {
}
