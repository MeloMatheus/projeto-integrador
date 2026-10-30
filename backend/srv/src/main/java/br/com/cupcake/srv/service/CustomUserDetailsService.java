package br.com.cupcake.srv.service;

import br.com.cupcake.srv.model.UsuarioModel;
import br.com.cupcake.srv.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuarioModel usuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
        return User.withUsername(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().getNome()))
                .build();
    }
}
