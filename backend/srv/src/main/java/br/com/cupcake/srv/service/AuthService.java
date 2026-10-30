package br.com.cupcake.srv.service;

import br.com.cupcake.srv.model.UsuarioModel;
import br.com.cupcake.srv.repository.PerfilRepository;
import br.com.cupcake.srv.repository.UsuarioRepository;
import br.com.cupcake.srv.request.auth.RegistroRequest;
import br.com.cupcake.srv.response.UsuarioResponse;
import br.com.cupcake.srv.response.auth.AuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final long expirationSeconds;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            UsuarioRepository usuarioRepository,
            PerfilRepository perfilRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.jwt.expiration}") long expirationSeconds
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
        if (expirationSeconds <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION deve ser maior que zero.");
        }
        this.expirationSeconds = expirationSeconds;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com este e-mail.");
        }
        var cliente = perfilRepository.findByNomeIgnoreCase("CLIENTE")
                .orElseThrow(() -> new IllegalStateException("Perfil CLIENTE ausente; execute as migrações."));
        UsuarioModel usuario = new UsuarioModel();
        usuario.setName(request.nome().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.senha()));
        usuario.setTelefone(request.telefone());
        usuario.setAtivo(true);
        usuario.setPerfil(cliente);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public AuthResponse autenticar(String email, String senha) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(normalizedEmail, senha)
        );
        UsuarioModel usuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas."));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("cupcake-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(authentication.getName())
                .claim("uid", usuario.getId().toString())
                .claim("roles", List.of("ROLE_" + usuario.getPerfil().getNome()))
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims
        )).getTokenValue();
        return new AuthResponse(token, "Bearer", UsuarioResponse.from(usuario));
    }
}
