// src/main/java/com/cafesaas/security/JwtService.java
package com.cafesaas.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private Key signingKey() {
        // Keys.hmacShaKeyFor needs at least 256 bits (32 bytes) for HS256
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a JWT embedding userId, tenantId, and role.
     * The tenantId claim is what the auth filter cross-checks
     * against the X-Tenant-ID header to prevent token replay
     * across tenants.
     */
    public String generateToken(String userId, String tenantId, Role role) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("tenantId", tenantId)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException e) {
            return false; // Expired, tampered, wrong signature — all caught here
        }
    }
}