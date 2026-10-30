package br.com.cupcake.srv.service;

import br.com.cupcake.srv.model.PerfilModel;
import br.com.cupcake.srv.model.UsuarioModel;
import br.com.cupcake.srv.repository.PerfilRepository;
import br.com.cupcake.srv.repository.UsuarioRepository;
import br.com.cupcake.srv.request.UsuarioRequest;
import br.com.cupcake.srv.response.UsuarioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PerfilRepository perfilRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse criarUsuario(UsuarioRequest request) {
        String email = normalizeEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com este e-mail.");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha é obrigatória.");
        }

        UsuarioModel usuario = new UsuarioModel();
        usuario.setName(request.name().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setTelefone(request.telefone());
        usuario.setAtivo(request.ativo() == null || request.ativo());
        usuario.setPerfil(findProfile(request.idPerfil()));
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse editarUsuario(UUID id, UsuarioRequest request, boolean administrador) {
        UsuarioModel usuario = usuarioRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        String email = normalizeEmail(request.email());
        if (usuarioRepository.existsByEmailAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com este e-mail.");
        }

        usuario.setName(request.name().trim());
        usuario.setEmail(email);
        usuario.setTelefone(request.telefone());
        if (administrador) {
            if (request.ativo() != null) {
                usuario.setAtivo(request.ativo());
            }
            if (request.idPerfil() != null) {
                usuario.setPerfil(findProfile(request.idPerfil()));
            }
        }
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public void excluirUsuario(UUID id) {
        UsuarioModel usuario = usuarioRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        usuario.setAtivo(false);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID id) {
        return usuarioRepository.findByIdAndAtivoTrue(id)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(normalizeEmail(email))
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioRepository.findByAtivoTrue(pageable).map(UsuarioResponse::from);
    }

    @Transactional(readOnly = true)
    public UsuarioModel buscarAtivoPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    private PerfilModel findProfile(UUID id) {
        if (id == null) {
            return perfilRepository.findByNomeIgnoreCase("CLIENTE")
                    .orElseThrow(() -> new IllegalStateException("Perfil CLIENTE ausente; execute as migrações."));
        }
        return perfilRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil não encontrado."));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
