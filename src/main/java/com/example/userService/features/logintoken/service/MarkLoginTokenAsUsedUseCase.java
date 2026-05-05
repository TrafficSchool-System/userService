package com.example.userService.features.logintoken.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.userService.features.logintoken.entity.LoginToken;
import com.example.userService.features.logintoken.repository.LoginTokenRepository;

/**
 * USE CASE: Mark Login Token As Used
 * 
 * Business logic for marking login tokens as used.
 * Prevents token replay attacks by ensuring one-time use.
 * 
 * Responsibilities:
 * - Mark token entity as used
 * - Persist change directly via repository
 * 
 * This is a vertical slice use-case following clean architecture.
 * No HTTP concerns, pure business operation.
 */
@Service
public class MarkLoginTokenAsUsedUseCase {

    private static final Logger log = LoggerFactory.getLogger(MarkLoginTokenAsUsedUseCase.class);

    private final LoginTokenRepository loginTokenRepository;

    public MarkLoginTokenAsUsedUseCase(LoginTokenRepository loginTokenRepository) {
        this.loginTokenRepository = loginTokenRepository;
    }

    /**
     * Mark login token as used.
     * 
     * Ensures token can only be used once (replay attack prevention).
     * 
     * @param loginToken Token entity to mark as used
     */
    public void execute(LoginToken loginToken) {
        log.debug("🔒 Marking token as used for email: {}", loginToken.getEmail());

        // BUSINESS LOGIC: Set used flag
        loginToken.setUsed(true);

        // PERSISTENCE: Save updated token to database
        loginTokenRepository.save(loginToken);

        log.info("✅ Token marked as used for email: {}", loginToken.getEmail());
    }
}
