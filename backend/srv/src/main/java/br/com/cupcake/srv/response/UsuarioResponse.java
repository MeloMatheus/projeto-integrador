package br.com.cupcake.srv.response;

import br.com.cupcake.srv.model.UsuarioModel;

import java.util.UUID;

public record UsuarioResponse(
        UUID idUsuario,
        String nome,
        String email,
        String telefone,
        Boolean ativo,
        java.time.LocalDateTime criadoEm,
        java.time.LocalDateTime atualizadoEm,
        UUID idPerfil,
        String perfil
) {
    public static UsuarioResponse from(UsuarioModel usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getName(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getAtivo(),
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm(),
                usuario.getPerfil().getId(),
                usuario.getPerfil().getNome()
        );
    }
}
