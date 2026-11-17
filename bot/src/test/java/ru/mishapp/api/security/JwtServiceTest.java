package ru.mishapp.api.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import ru.mishapp.api.config.JwtProperties;
import ru.mishapp.entity.User;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private final JwtProperties properties = new JwtProperties(
            "01234567890123456789012345678901",
            Duration.ofMinutes(15),
            Duration.ofDays(30)
    );

    private JwtService jwtService;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        SecretKey key = new SecretKeySpec(
                properties.secret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        JWKSource<SecurityContext> source = new ImmutableSecret<>(key);
        jwtService = new JwtService(new NimbusJwtEncoder(source), properties);
        jwtDecoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Test
    void shouldIssueAccessTokenWithUserClaims() {
        User user = new User(42L, "user", "hash", "UTC", Set.of());

        String token = jwtService.issueAccessToken(user);
        Jwt jwt = jwtDecoder.decode(token);

        assertEquals("user", jwt.getSubject());
        assertEquals(42L, ((Number) jwt.getClaim("uid")).longValue());
        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());
        assertTrue(jwt.getExpiresAt().isAfter(Instant.now().plus(Duration.ofMinutes(14))));
    }

    @Test
    void shouldExposeTokenTtls() {
        assertEquals(900, jwtService.accessTtlSeconds());
        assertEquals(Duration.ofDays(30), jwtService.refreshTtl());
    }
}
