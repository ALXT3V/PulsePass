package com.pulsepass.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setIssuer("pulsepass-api-test");
        jwtProperties.setSecretBase64("c3VwZXItc2VjcmV0LWtleS1mb3ItcHVsc2VwYXNzLWFwaS1zZWN1cml0eS0yMDI2");
        jwtProperties.setAccessTokenTtl(Duration.ofMinutes(15));

        jwtService = new JwtService(jwtProperties);
    }

    @Test
    void shouldGenerateValidToken() {
        SecurityPrincipal principal = new SecurityPrincipal(
                100L,
                "andrea",
                "andrea@pulsepass.com",
                "hash",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtService.generateToken(principal);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(100L);
    }

    @Test
    void shouldRejectInvalidToken() {
        String tamperedToken = "eyJhbGciOiJIUzI1NiJ9.invalidpayload.signature";
        assertThat(jwtService.isTokenValid(tamperedToken)).isFalse();
    }
}