package com.freelanceplatform.security;

import com.freelanceplatform.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long tokenMinutes;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.access-token-minutes:30}") long tokenMinutes) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 32 bytes");
        }
        if (tokenMinutes < 1 || tokenMinutes > 1440) {
            throw new IllegalArgumentException("JWT access token lifetime must be between 1 and 1440 minutes");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenMinutes = tokenMinutes;
    }

    public String createToken(UserAccount user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getEmail())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(tokenMinutes * 60)))
                .signWith(key).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long expiresInSeconds() { return tokenMinutes * 60; }
}
