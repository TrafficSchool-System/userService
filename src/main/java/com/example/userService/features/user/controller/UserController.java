package com.example.userService.features.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userService.features.user.dto.RegisterRequestDTO;
import com.example.userService.features.user.dto.UserResponseDTO;
import com.example.userService.features.user.service.RegisterUserUseCase;
import com.example.userService.features.user.service.GetUserUseCase;
import com.example.userService.shared.security.CustomUserAuthentication;

import jakarta.validation.Valid;

/**
 * User Controller
 * 
 * PUBLIC and AUTHENTICATED endpoints for user registration and profile access.
 * 
 * AUTHENTICATION PATTERN (Gateway-First):
 * - API Gateway validates JWT and sets X-User-* headers
 * - GatewayHeaderAuthenticationFilter creates CustomUserAuthentication
 * - SecurityContext is the SINGLE SOURCE OF TRUTH
 * - Controllers use @AuthenticationPrincipal (Spring Security standard)
 * - NO manual header parsing
 * 
 * Uses constructor injection (no @Autowired).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterUserUseCase registerUserService;
    private final GetUserUseCase getUserService;

    public UserController(
            RegisterUserUseCase registerUserService,
            GetUserUseCase getUserService) {
        this.registerUserService = registerUserService;
        this.getUserService = getUserService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody RegisterRequestDTO request) {
        UserResponseDTO user = registerUserService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * GET CURRENT USER PROFILE
     * GET /api/users/me
     * 
     * Returns the authenticated user's profile information.
     * 
     * AUTHENTICATION:
     * - Uses @AuthenticationPrincipal to get CustomUserAuthentication from
     * SecurityContext
     * - NO manual header parsing
     * - SecurityContext is the single source of truth
     * 
     * @param auth Authenticated user from SecurityContext
     * @return Current user profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @AuthenticationPrincipal CustomUserAuthentication auth) {
        Long userId = auth.getUserId();
        return ResponseEntity.ok(getUserService.findById(userId));
    }

}
