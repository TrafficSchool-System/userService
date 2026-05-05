package com.example.userService.features.logintoken.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.userService.features.logintoken.entity.LoginToken;
import com.example.userService.features.logintoken.repository.LoginTokenRepository;

/**
 * USE CASE: Create Login Token
 * 
 * Business logic for creating login tokens (magic links).
 * Handles token generation, expiration calculation, and persistence.
 * 
 * Responsibilities:
 * - Generate unique token (UUID)
 * - Calculate expiration time
 * - Create LoginToken entity
 * - Persist directly via repository
 * 
 * This is a vertical slice use-case following clean architecture.
 * No HTTP concerns, pure business orchestration.
 */
@Service
public class CreateLoginTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateLoginTokenUseCase.class);

    private final LoginTokenRepository loginTokenRepository;

    public CreateLoginTokenUseCase(LoginTokenRepository loginTokenRepository) {
        this.loginTokenRepository = loginTokenRepository;
    }

    /**
     * Create login token with specified validity period.
     * 
     * @param email        User's email address
     * @param minutesValid Token validity in minutes
     * @return Generated token string (UUID)
     */
    public String execute(String email, int minutesValid) {
        log.debug("🔑 Creating login token for: {} (valid for {} minutes)", email, minutesValid);

        // BUSINESS LOGIC: Generate unique token
        String tokenString = UUID.randomUUID().toString();

        // BUSINESS LOGIC: Calculate expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(minutesValid);
        LocalDateTime createdAt = LocalDateTime.now();

        // BUSINESS LOGIC: Create token entity
        LoginToken loginToken = new LoginToken(tokenString, email, expiresAt, createdAt);

        // PERSISTENCE: Save token to database
        loginTokenRepository.save(loginToken);

        log.info("✅ Login token created for: {} (expires at: {})", email, expiresAt);

        return tokenString;
    }

    /**
     * Create welcome token for new user (30 minutes validity).
     * 
     * @param email User's email address
     * @return Generated token string (UUID)
     */
    public String executeWelcomeToken(String email) {
        log.debug("📧 Creating welcome token for new user: {}", email);
        return execute(email, 30);
    }
}
