package com.example.userService.features.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userService.features.auth.dto.AuthUserContext;
import com.example.userService.features.auth.dto.JwtResponseDTO;
import com.example.userService.features.auth.dto.LoginRequestDTO;
import com.example.userService.features.auth.dto.VerifyTokenRequestDTO;
import com.example.userService.features.auth.service.CreateMagicLinkUseCase;
import com.example.userService.features.auth.service.GenerateJwtUseCase;
import com.example.userService.features.auth.service.VerifyMagicLinkUseCase;

import jakarta.validation.Valid;

/**
 * ============================================================================
 * AUTHENTICATION CONTROLLER - Magic Link Flow (Clean Architecture)
 * ============================================================================
 * 
 * PUBLIC endpoints for user authentication via magic link.
 * No JWT required for these endpoints.
 * 
 * FLOW:
 * 1. POST /auth/login → Request magic link via email
 * 2. POST /auth/verify → Verify token (returns minimal auth context)
 * 3. POST /auth/tokens → Verify token & receive JWT (recommended)
 * 
 * RESTful naming: /tokens represents creating a JWT token resource
 * 
 * CLEAN ARCHITECTURE (Vertical Slice):
 * - Controller calls use-cases DIRECTLY (no service layer)
 * - Zero business logic in controller
 * - Minimal orchestration only
 * - Use-cases contain all business logic
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CreateMagicLinkUseCase createMagicLinkUseCase;
    private final VerifyMagicLinkUseCase verifyMagicLinkUseCase;
    private final GenerateJwtUseCase generateJwtUseCase;

    public AuthController(
            CreateMagicLinkUseCase createMagicLinkUseCase,
            VerifyMagicLinkUseCase verifyMagicLinkUseCase,
            GenerateJwtUseCase generateJwtUseCase) {
        this.createMagicLinkUseCase = createMagicLinkUseCase;
        this.verifyMagicLinkUseCase = verifyMagicLinkUseCase;
        this.generateJwtUseCase = generateJwtUseCase;
    }

    /**
     * REQUEST MAGIC LINK
     * POST /api/auth/login
     * 
     * Sends a magic link to the user's email for passwordless authentication.
     * Returns success message if email exists in system.
     */
    @PostMapping("/login")
    public ResponseEntity<String> requestMagicLink(@Valid @RequestBody LoginRequestDTO request) {
        createMagicLinkUseCase.execute(request);
        return ResponseEntity.ok("Magic link skickat till: " + request.getEmail());
    }

    /**
     * VERIFY MAGIC LINK (Basic)
     * POST /api/auth/verify
     * 
     * Verifies magic link token and returns minimal auth context.
     * Returns only essential identity data (userId, email, role).
     * 
     * For full user data, use /api/users endpoints after authentication.
     * For JWT authentication, use POST /api/auth/tokens instead.
     */
    @PostMapping("/verify")
    public ResponseEntity<AuthUserContext> verifyToken(@Valid @RequestBody VerifyTokenRequestDTO request) {
        return ResponseEntity.ok(verifyMagicLinkUseCase.execute(request));
    }

    /**
     * VERIFY AND GET JWT TOKEN
     * POST /api/auth/tokens
     * 
     * Verifies magic link token and creates a JWT token for the user.
     * Returns JWT token that should be used for subsequent authenticated requests.
     * 
     * RESTful: Creating a token resource, hence POST /tokens
     */
    @PostMapping("/tokens")
    public ResponseEntity<JwtResponseDTO> createJwtToken(@Valid @RequestBody VerifyTokenRequestDTO request) {
        AuthUserContext auth = verifyMagicLinkUseCase.execute(request);
        return ResponseEntity.ok(generateJwtUseCase.execute(auth));
    }
}