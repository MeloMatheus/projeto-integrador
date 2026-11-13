package br.com.cupcake.srv;

import br.com.cupcake.srv.model.PerfilModel;
import br.com.cupcake.srv.repository.PerfilRepository;
import br.com.cupcake.srv.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @BeforeEach
    void ensureClientProfile() {
        perfilRepository.findByNomeIgnoreCase("CLIENTE").orElseGet(() -> {
            PerfilModel profile = new PerfilModel();
            profile.setNome("CLIENTE");
            return perfilRepository.save(profile);
        });
    }

    @Test
    void registrationAlwaysCreatesClientAndNeverReturnsPasswordHash() throws Exception {
        String email = "client-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "Cliente Teste",
                                "email", email,
                                "senha", "password-123",
                                "perfil", "ADMIN"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("CLIENTE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        var user = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(email).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(user.getPassword().startsWith("$2"));
        org.junit.jupiter.api.Assertions.assertEquals(
                "CLIENTE",
                perfilRepository.findById(user.getPerfil().getId()).orElseThrow().getNome()
        );
    }

    @Test
    void loginReturnsBearerTokenAndProtectedResourcesRequireIt() throws Exception {
        String email = "login-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "nome", "Cliente",
                        "email", email,
                        "senha", "password-123"
                ))));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "senha", "password-123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.token", startsWith("eyJ")));

        mockMvc.perform(get("/api/carrinho/{idUsuario}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void inactiveUsersCannotAuthenticate() throws Exception {
        String email = "inactive-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "nome", "Conta inativa",
                        "email", email,
                        "senha", "password-123"
                )))).andExpect(status().isCreated());

        var user = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(email).orElseThrow();
        user.setAtivo(false);
        usuarioRepository.save(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "senha", "password-123"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void administradorRoleGrantsAdminAuthority() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("admin@example.com")
                .claim("roles", List.of("ROLE_ADMINISTRADOR"))
                .build();

        var authentication = jwtAuthenticationConverter.convert(jwt);

        org.junit.jupiter.api.Assertions.assertNotNull(authentication);
        org.junit.jupiter.api.Assertions.assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }
}
