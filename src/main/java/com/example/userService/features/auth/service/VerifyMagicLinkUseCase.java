package com.example.userService.features.auth.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.userService.features.auth.dto.AuthUserContext;
import com.example.userService.features.auth.dto.VerifyTokenRequestDTO;
import com.example.userService.features.logintoken.entity.LoginToken;
import com.example.userService.features.logintoken.service.MarkLoginTokenAsUsedUseCase;
import com.example.userService.features.logintoken.service.ValidateLoginTokenUseCase;
import com.example.userService.features.subscription.service.HasActiveSubscriptionUseCase;
import com.example.userService.features.user.dto.UserResponseDTO;
import com.example.userService.features.user.service.GetUserUseCase;
import com.example.userService.shared.exception.InvalidTokenException;

import jakarta.transaction.Transactional;

/**
 * USE CASE: Verify Magic Link Token
 * 
 * Handles the complete magic link verification flow:
 * 1. Find and validate login token exists
 * 2. Check token expiration
 * 3. Verify token hasn't been used
 * 4. Mark token as used (one-time use)
 * 5. Retrieve authenticated user
 * 6. Map to AuthUserContext (decouple from User domain)
 * 
 * This is a vertical slice service following clean architecture.
 * No HTTP concerns, no JWT generation, only token verification logic.
 * Returns AuthUserContext to decouple Auth domain from User domain.
 */
@Service
public class VerifyMagicLinkUseCase {

    private static final Logger log = LoggerFactory.getLogger(VerifyMagicLinkUseCase.class);

    private final ValidateLoginTokenUseCase validateLoginTokenUseCase;
    private final MarkLoginTokenAsUsedUseCase markLoginTokenAsUsedUseCase;
    private final GetUserUseCase getUserService;
    private final HasActiveSubscriptionUseCase hasActiveSubscriptionUseCase;

    public VerifyMagicLinkUseCase(
            ValidateLoginTokenUseCase validateLoginTokenUseCase,
            MarkLoginTokenAsUsedUseCase markLoginTokenAsUsedUseCase,
            GetUserUseCase getUserService,
            HasActiveSubscriptionUseCase hasActiveSubscriptionUseCase) {
        this.validateLoginTokenUseCase = validateLoginTokenUseCase;
        this.markLoginTokenAsUsedUseCase = markLoginTokenAsUsedUseCase;
        this.getUserService = getUserService;
        this.hasActiveSubscriptionUseCase = hasActiveSubscriptionUseCase;
    }

    /**
     * Verify magic link token and return authenticated user context.
     * 
     * Validates token existence, expiry, and usage status.
     * Marks token as used to prevent replay attacks.
     * Returns minimal auth context (decoupled from User domain).
     * 
     * @param request Verification request containing token string
     * @return Auth user context with minimal identity data
     * @throws InvalidTokenException if token is invalid, expired, or already used
     */
    @Transactional
    public AuthUserContext execute(VerifyTokenRequestDTO request) {

        String tokenString = request.getToken();

        log.debug("Verifying magic link token");

        // Validate token (business logic delegated to use case)
        LoginToken loginToken = validateLoginTokenUseCase.execute(tokenString);

        // Mark token as used (one-time use security)
        markLoginTokenAsUsedUseCase.execute(loginToken);
        log.debug("Token marked as used");

        // Get authenticated user from User domain
        UserResponseDTO user = getUserService.findByEmail(loginToken.getEmail());

        // Check if user has active subscription
        boolean hasActiveSubscription = hasActiveSubscriptionUseCase.execute(user.getId());
        log.debug("User {} hasActiveSubscription: {}", user.getId(), hasActiveSubscription);

        // Map to Auth domain context (decouple from User domain)
        AuthUserContext authContext = new AuthUserContext(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                hasActiveSubscription);

        log.info("Magic link verified successfully for userId={} hasActiveSubscription={}",
                authContext.userId(), hasActiveSubscription);

        return authContext;
    }
}
