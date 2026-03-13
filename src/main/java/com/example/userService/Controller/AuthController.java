package com.example.userService.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userService.Dto.JwtResponseDTO;
import com.example.userService.Dto.LoginRequestDTO;
import com.example.userService.Dto.UserResponseDTO;
import com.example.userService.Dto.VerifyTokenRequestDTO;
import com.example.userService.Service.auth.AuthServiceInterface;

import jakarta.validation.Valid;

/**
 * ============================================================================
 * AUTHENTICATION CONTROLLER - Magic Link Flow
 * ============================================================================
 * 
 * PUBLIC endpoints for user authentication via magic link.
 * No JWT required for these endpoints.
 * 
 * FLOW:
 * 1. POST /auth/login → Request magic link via email
 * 2. POST /auth/verify → Verify token (basic verification)
 * 3. POST /auth/tokens → Verify token & receive JWT (recommended)
 * 
 * RESTful naming: /tokens represents creating a JWT token resource
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthServiceInterface authService;

    /**
     * REQUEST MAGIC LINK
     * POST /api/auth/login
     * 
     * Sends a magic link to the user's email for passwordless authentication.
     * Returns success message if email exists in system.
     */
    @PostMapping("/login")
    public ResponseEntity<String> requestMagicLink(@Valid @RequestBody LoginRequestDTO request) {
        authService.createMagicLink(request);
        return ResponseEntity.ok("Magic link skickat till: " + request.getEmail());
    }

    /**
     * VERIFY MAGIC LINK (Basic)
     * POST /api/auth/verify
     * 
     * Verifies magic link token and returns user data.
     * Use /auth/tokens instead for JWT-based authentication.
     */
    @PostMapping("/verify")
    public ResponseEntity<UserResponseDTO> verifyToken(@Valid @RequestBody VerifyTokenRequestDTO request) {
        UserResponseDTO user = authService.verifyMagicLink(request);
        return ResponseEntity.ok(user);
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
        JwtResponseDTO jwtResponse = authService.verifyMagicLinkWithJwt(request);
        return ResponseEntity.ok(jwtResponse);
    }
} 