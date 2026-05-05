package com.example.userService.features.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.userService.features.auth.dto.LoginRequestDTO;
import com.example.userService.features.email.usecase.SendMagicLinkEmailUseCase;
import com.example.userService.features.logintoken.repository.LoginTokenRepository;
import com.example.userService.features.logintoken.service.CreateLoginTokenUseCase;
import com.example.userService.features.user.dto.UserResponseDTO;
import com.example.userService.features.user.service.GetUserUseCase;

import jakarta.transaction.Transactional;

/**
 * USE CASE: Create Magic Link for Passwordless Login
 * 
 * Orchestrates the complete flow for generating a magic link:
 * 1. Validate user exists (via GetUserService)
 * 2. Clean up old tokens for email
 * 3. Generate new login token
 * 4. Send personalized email with magic link
 * 
 * This is a vertical slice service following clean architecture.
 * No HTTP concerns, only pure orchestration logic.
 * GetUserService handles user existence validation.
 */
@Service
public class CreateMagicLinkUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateMagicLinkUseCase.class);

    private final GetUserUseCase getUserService;
    private final LoginTokenRepository loginTokenRepository;
    private final CreateLoginTokenUseCase createLoginTokenUseCase;
    private final SendMagicLinkEmailUseCase sendMagicLinkEmailUseCase;

    public CreateMagicLinkUseCase(
            GetUserUseCase getUserService,
            LoginTokenRepository loginTokenRepository,
            CreateLoginTokenUseCase createLoginTokenUseCase,
            SendMagicLinkEmailUseCase sendMagicLinkEmailUseCase) {
        this.getUserService = getUserService;
        this.loginTokenRepository = loginTokenRepository;
        this.createLoginTokenUseCase = createLoginTokenUseCase;
        this.sendMagicLinkEmailUseCase = sendMagicLinkEmailUseCase;
    }

    /**
     * Create and send magic link for passwordless authentication.
     * 
     * Orchestrates token creation and email delivery.
     * User validation is delegated to GetUserService (throws UserNotFoundException
     * if not found).
     * 
     * @param request Login request containing user email
     * @return Generated token string
     */
    @Transactional
    public String execute(LoginRequestDTO request) {

        String email = request.getEmail();

        // GetUserService validates existence and throws UserNotFoundException if not
        // found
        UserResponseDTO user = getUserService.findByEmail(email);
        log.debug("Creating magic link for user id={}", user.getId());

        // Clean up old tokens before creating new one
        loginTokenRepository.deleteByEmailAndUsedFalse(email);
        log.debug("Deleted old unused tokens for user id={}", user.getId());

        // Create new token (5 minute expiry)
        String token = createLoginTokenUseCase.execute(email, 5);
        log.debug("New login token created for user id={}", user.getId());

        // Send magic link email
        sendMagicLinkEmailUseCase.execute(email, user.getFirstName(), token);
        log.info("Magic link sent successfully for user id={}", user.getId());

        return token;
    }
}
