package com.example.userService.features.logintoken.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.userService.features.logintoken.entity.LoginToken;
import com.example.userService.features.logintoken.repository.LoginTokenRepository;
import com.example.userService.shared.exception.InvalidTokenException;

/**
 * USE CASE: Validate Login Token
 * 
 * Business logic for validating login tokens (magic links).
 * Handles token existence, expiration, and usage validation.
 * 
 * Responsibilities:
 * - Find token by value
 * - Validate token exists
 * - Validate token not expired
 * - Validate token not already used
 * - Return valid LoginToken entity
 * 
 * This is a vertical slice use-case following clean architecture.
 * No HTTP concerns, pure business validation logic.
 */
@Service
public class ValidateLoginTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(ValidateLoginTokenUseCase.class);

    private final LoginTokenRepository loginTokenRepository;

    public ValidateLoginTokenUseCase(LoginTokenRepository loginTokenRepository) {
        this.loginTokenRepository = loginTokenRepository;
    }

    /**
     * Validate login token.
     * 
     * Checks:
     * 1. Token exists and is unused
     * 2. Token has not expired
     * 3. Token is not marked as used (defensive check)
     * 
     * @param tokenString Token value to validate
     * @return Valid LoginToken entity
     * @throws InvalidTokenException if token is invalid, expired, or already used
     */
    public LoginToken execute(String tokenString) {
        log.debug("Validating login token");

        // BUSINESS LOGIC: Find unused token
        LoginToken loginToken = loginTokenRepository.findByTokenAndUsedFalse(tokenString)
                .orElseThrow(() -> {
                    log.warn("Token validation failed: token not found or already used");
                    return new InvalidTokenException("Invalid or already used link");
                });

        // BUSINESS LOGIC: Check token expiration
        if (LocalDateTime.now().isAfter(loginToken.getExpiresAt())) {
            log.warn("Token validation failed: token expired at {}", loginToken.getExpiresAt());
            throw new InvalidTokenException("Link has expired, request a new link");
        }

        // BUSINESS LOGIC: Extra defensive check (should already be filtered by
        // findUnusedToken)
        if (loginToken.isUsed()) {
            log.warn("Token validation failed: token already marked as used");
            throw new InvalidTokenException("Link has already been used");
        }

        log.debug("Token validated successfully for user email domain: {}",
                loginToken.getEmail().replaceAll("^[^@]+", "***"));

        return loginToken;
    }
}
