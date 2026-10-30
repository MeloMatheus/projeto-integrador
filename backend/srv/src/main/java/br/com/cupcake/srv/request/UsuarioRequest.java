package br.com.cupcake.srv.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UsuarioRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @Size(min = 8, max = 72)
        String password,
        String telefone,
        Boolean ativo,
        UUID idPerfil
) {
}
