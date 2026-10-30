package br.com.cupcake.srv.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfiguration {

    @Bean
    SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Configure JWT_SECRET com uma chave Base64 de pelo menos 32 bytes.");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET deve estar codificada em Base64.", exception);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET deve decodificar para pelo menos 32 bytes.");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("cupcake-api"));
        return decoder;
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }
}
