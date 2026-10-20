package com.orbit.sentinel.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Verifies tokens issued by orbit-graduate-system. It never issues tokens itself. */
@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${sentinel.jwt.secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters");
        }
        if (secret.startsWith("paste-the-same") || secret.contains("change-me")) {
            throw new IllegalStateException("Set JWT_SECRET in .env to the same value Orbit uses");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer("orbit")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
