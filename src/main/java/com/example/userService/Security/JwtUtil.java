package com.example.userService.Security;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    // Secret key från application properties
    @Value("${jwt.secret}")
    private String secret;

    // Tokens giltighet i millisekunder(24 timmar)
    @Value("${jwt.expiration}")
    private Long expiration;

    // Skapar en säker nyckel från secret string
    // Nyckeln används för att signera och verifera token
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Genererar en JWT token för en användare (deprecated - använd versionen med
    // role)
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email);
    }

    // Genererar en JWT token med role (BEST PRACTICE)
    public String generateToken(String email, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        // Token innehåller roller UTAN "ROLE_" prefix (Spring Security standard)
        claims.put("roles", Collections.singletonList(role.toUpperCase())); // "USER" eller "ADMIN"
        claims.put("role", role); // Behåll för bakåtkompatibilitet
        claims.put("userId", userId); // Lägg till userId i token
        return createToken(claims, email);
    }

    /**
     * Skapar själva JWT token med:
     * Claims: Extra data vi vill ha i token
     * Subject: Användarens email
     * IssuedAt: När token skapades
     * Expiration: När token går ut
     * Signature: Säkerhetsstämpel
     */

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims) // Extra data
                .setSubject(subject) // användarens email
                .setIssuedAt(new Date(System.currentTimeMillis())) // Skapad nu
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey()) // Signera med secret key
                .compact(); // Bygg token som string
    }

    // Extrahera användarens email från en JWT token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrahera role från token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Extrahera userId från token
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    // Extrahera expiration date från token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generisk metod för att extrahera data från token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extrahera all data från token, Här valideras också token signaturen
    // automatiskt
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Verifera signatur
                .build()
                .parseClaimsJws(token) // Parsa token
                .getBody(); // Hämta data
    }

    // Kollar om token har gått ut
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validera en JWT token mot en email
     * Kollar att:
     * 1. Email i token matchar med förväntat email
     * 2. Token inte har gått ut
     */

    public Boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    /**
     * Enkel validering - kollar bara om token är giltig
     * Används när vi inte har en specifik email att jämföra mot
     */
    private Boolean isValidToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false; // Token är trasig eller ogiltig
        }
    }

}
