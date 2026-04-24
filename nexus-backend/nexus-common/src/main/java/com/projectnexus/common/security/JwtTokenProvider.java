package com.projectnexus.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT token generation and validation for Project-Nexus.
 *
 * <p>Tokens carry three critical claims:
 * <ul>
 *   <li>{@code tenant_id} — used for PostgreSQL RLS and application-level tenant isolation</li>
 *   <li>{@code user_id} (the subject) — identifies the authenticated user</li>
 *   <li>{@code roles} — list of RBAC roles for authorization decisions</li>
 * </ul>
 *
 * <p>The signing key is configured via {@code nexus.security.jwt.secret} and must be
 * at least 256 bits (32 characters) for HMAC-SHA256.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long expirationHours;

    private static final int MIN_SECRET_LENGTH = 32;

    public JwtTokenProvider(
            @Value("${nexus.security.jwt.secret:default-dev-secret-key-change-in-production-min-32-chars}") String secret,
            @Value("${nexus.security.jwt.expiration-hours:24}") long expirationHours) {
        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least " + MIN_SECRET_LENGTH + " characters for HMAC-SHA256");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationHours = expirationHours;
    }

    /**
     * Generates a signed JWT containing user, tenant, and role claims.
     *
     * @param userId   the authenticated user's UUID
     * @param tenantId the user's tenant UUID
     * @param roles    the user's RBAC roles
     * @return a signed JWT string
     */
    public String generateToken(UUID userId, UUID tenantId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("tenant_id", tenantId.toString())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationHours, ChronoUnit.HOURS)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates the token signature and expiration.
     *
     * @param token the JWT string
     * @return {@code true} if valid, {@code false} otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the tenant UUID from the token's {@code tenant_id} claim.
     */
    public UUID extractTenantId(String token) {
        return UUID.fromString(parseClaims(token).get("tenant_id", String.class));
    }

    /**
     * Extracts the user UUID from the token's subject claim.
     */
    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    /**
     * Extracts the RBAC roles from the token's {@code roles} claim.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return parseClaims(token).get("roles", List.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
