package com.example.userService.shared.security;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT TOKEN GENERATOR (UserService)
 * 
 * GATEWAY-FIRST ARCHITECTURE:
 * - UserService ONLY GENERATES tokens (after magic link verification)
 * - API Gateway VALIDATES tokens (on every request)
 * - Microservices NEVER validate JWT (they trust Gateway headers)
 * 
 * RESPONSIBILITY:
 * - Generate JWT tokens for authenticated users
 * - Token contains: email (subject), userId, role
 * - Shared secret with API Gateway (must be identical)
 * 
 * DO NOT ADD:
 * - Token validation methods (Gateway's responsibility)
 * - Token parsing methods (Gateway's responsibility)
 * - extractEmail/extractRole/extractUserId (dead code - never used here)
 * 
 * SECURITY:
 * - Secret key MUST match API Gateway's secret
 * - Expiration time SHOULD match API Gateway's config
 * - Store secret in environment variable (NOT hardcoded)
 */
@Component
public class JwtUtil {

    // Secret key from application properties
    // MUST be identical to API Gateway's secret
    @Value("${jwt.secret}")
    private String secret;

    // Token validity in milliseconds (24 hours)
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Create a secure signing key from secret string.
     * The key is used to sign tokens (NOT verify - Gateway does that).
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generate a JWT token for an authenticated user.
     * 
     * Called after successful magic link verification.
     * 
     * Token contents:
     * - subject: User email
     * - userId: User ID (Long)
     * - role: User role (USER or ADMIN)
     * - roles: Array of roles (for Spring Security compatibility)
     * - issuedAt: Token creation timestamp
     * - expiration: Token expiration timestamp
     * 
     * @param email  User's email address
     * @param role   User's role (USER or ADMIN)
     * @param userId User's ID
     * @return Signed JWT token string
     */
    public String generateToken(String email, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();

        // Token contains roles WITHOUT "ROLE_" prefix (Spring Security standard)
        // Gateway will add "ROLE_" prefix when creating authorities
        claims.put("role", role.toUpperCase()); // "USER" or "ADMIN"
        claims.put("roles", Collections.singletonList(role.toUpperCase())); // For compatibility
        claims.put("userId", userId); // User ID for direct access

        return createToken(claims, email);
    }

    /**
     * Create the actual JWT token with:
     * - Claims: Extra data we want in the token
     * - Subject: User's email
     * - IssuedAt: When token was created
     * - Expiration: When token expires
     * - Signature: Security stamp
     * 
     * @param claims  Custom claims to include in token
     * @param subject User's email (subject of the token)
     * @return Signed JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims) // Extra data
                .setSubject(subject) // User's email
                .setIssuedAt(new Date(System.currentTimeMillis())) // Created now
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Expires in 24h
                .signWith(getSigningKey()) // Sign with secret key
                .compact(); // Build token as string
    }

    /**
     * DEPRECATED: Use the version with role and userId.
     * Kept for backward compatibility only.
     * 
     * @deprecated Use {@link #generateToken(String, String, Long)} instead
     */
    @Deprecated
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email);
    }
}
