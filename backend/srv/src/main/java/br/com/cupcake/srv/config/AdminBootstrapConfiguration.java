package br.com.cupcake.srv.config;

import br.com.cupcake.srv.model.UsuarioModel;
import br.com.cupcake.srv.repository.PerfilRepository;
import br.com.cupcake.srv.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Configuration
public class AdminBootstrapConfiguration {

    @Bean
    CommandLineRunner bootstrapAdmin(
            UsuarioRepository usuarioRepository,
            PerfilRepository perfilRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.email:}") String configuredEmail,
            @Value("${app.bootstrap.admin.password:}") String configuredPassword
    ) {
        return args -> {
            if (configuredEmail.isBlank() && configuredPassword.isBlank()) {
                return;
            }
            if (configuredEmail.isBlank() || configuredPassword.isBlank()) {
                throw new IllegalStateException("INITIAL_ADMIN_EMAIL e INITIAL_ADMIN_PASSWORD devem ser informados juntos.");
            }
            String email = configuredEmail.trim().toLowerCase(Locale.ROOT);
            if (!email.contains("@") || email.length() > 180
                    || configuredPassword.length() < 12 || configuredPassword.length() > 72) {
                throw new IllegalStateException("Dados inválidos para provisionar o administrador inicial.");
            }
            if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
                return;
            }
            var adminProfile = perfilRepository.findByNomeIgnoreCase("ADMIN")
                    .orElseThrow(() -> new IllegalStateException("Perfil ADMIN ausente; execute as migrações."));
            UsuarioModel admin = new UsuarioModel();
            admin.setName("Administrador");
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(configuredPassword));
            admin.setPerfil(adminProfile);
            admin.setAtivo(true);
            usuarioRepository.save(admin);
        };
    }
}
