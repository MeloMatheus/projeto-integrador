package br.com.cupcake.srv.response.auth;

import br.com.cupcake.srv.response.UsuarioResponse;

public record AuthResponse(String token, String tipo, UsuarioResponse usuario) {
}
