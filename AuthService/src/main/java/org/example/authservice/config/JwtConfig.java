package org.example.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${app.jwt.private-key:classpath:keys/private.pem}")
    private Resource privateKeyResource;

    @Value("${app.jwt.expiration:3600000}")
    private Long jwtExpiration;

    @Value("${app.jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    @Bean
    public PrivateKey jwtPrivateKey() {
        try {
            String privateKeyPem = new String(privateKeyResource.getInputStream().readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPem);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JWT private key", e);
        }
    }

    @Bean
    public Long jwtExpiration() {
        return jwtExpiration;
    }

    @Bean
    public Long refreshExpiration() {
        return refreshExpiration;
    }
}