package br.com.cupcake.srv.controller;

import br.com.cupcake.srv.request.UsuarioRequest;
import br.com.cupcake.srv.response.UsuarioResponse;
import br.com.cupcake.srv.service.UsuarioService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioService.listar(pageOf(pageable));
    }

    @GetMapping("/me")
    public UsuarioResponse meuPerfil(Authentication authentication) {
        return usuarioService.buscarPorEmail(authentication.getName());
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable UUID id, Authentication authentication) {
        UsuarioResponse usuario = usuarioService.buscarPorId(id);
        if (!isAdmin(authentication) && !usuario.email().equalsIgnoreCase(authentication.getName())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }
        return usuario;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> criarUsuario(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse usuario = usuarioService.criarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @PutMapping("/{id}")
    public UsuarioResponse editarUsuario(
            @PathVariable UUID id,
            @Valid @RequestBody UsuarioRequest request,
            Authentication authentication
    ) {
        UsuarioResponse current = usuarioService.buscarPorId(id);
        boolean admin = isAdmin(authentication);
        if (!admin && !current.email().equalsIgnoreCase(authentication.getName())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }
        return usuarioService.editarUsuario(id, request, admin);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluirUsuario(@PathVariable UUID id) {
        usuarioService.excluirUsuario(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private Pageable pageOf(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), pageable.getSort());
    }
}
