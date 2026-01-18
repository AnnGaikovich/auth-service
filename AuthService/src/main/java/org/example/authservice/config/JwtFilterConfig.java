package org.example.authservice.config;

import org.example.authservice.auth.filter.JwtAuthenticationFilter;
import org.example.authservice.auth.util.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtFilterConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider(java.security.PublicKey publicKey) {
        return new JwtTokenProvider(publicKey);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }
}