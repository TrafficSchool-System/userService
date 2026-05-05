package com.example.userService.features.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.userService.features.auth.dto.AuthUserContext;
import com.example.userService.features.auth.dto.JwtResponseDTO;
import com.example.userService.shared.security.JwtUtil;

/**
 * USE CASE: Generate JWT Token
 * 
 * Handles JWT token generation for authenticated users.
 * Takes minimal auth context and generates a signed JWT token containing:
 * - User email (subject)
 * - User role (authority)
 * - User ID (claim)
 * 
 * This is a vertical slice service following clean architecture.
 * No HTTP concerns, no validation, only token generation logic.
 * Assumes user is already authenticated and validated.
 * 
 * Uses AuthUserContext to decouple from User domain.
 */
@Service
public class GenerateJwtUseCase {

    private static final Logger log = LoggerFactory.getLogger(GenerateJwtUseCase.class);

    private final JwtUtil jwtUtil;

    public GenerateJwtUseCase(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Generate JWT token for authenticated user.
     * 
     * Creates a signed JWT containing user email, role, and ID.
     * Returns complete response with token and auth context.
     * 
     * @param authContext Authenticated user context (minimal identity data)
     * @return JWT response containing token and auth context
     */
    public JwtResponseDTO execute(AuthUserContext authContext) {

        log.debug("Generating JWT for user: {}", authContext.email());

        // Generate JWT token with user claims
        String jwtToken = jwtUtil.generateToken(
                authContext.email(),
                authContext.role(),
                authContext.userId());

        log.info("JWT generated successfully for user: {} (ID: {})", authContext.email(), authContext.userId());

        // Return complete response with token and auth context
        return new JwtResponseDTO(jwtToken, authContext);
    }
}
