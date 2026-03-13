package com.example.userService.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.userService.Dto.RegisterRequestDTO;
import com.example.userService.Dto.UpdateUserRequestDTO;
import com.example.userService.Dto.UserResponseDTO;
import com.example.userService.Security.JwtUtil;
import com.example.userService.Service.user.UserServiceInterface;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * USER CONTROLLER
 * 
 * RESTful endpoints for user profile operations.
 * Base path: /api/users
 * 
 * USER OPERATIONS:
 * - POST /users : Create new user account (registration)
 * - GET /users/me : Get current authenticated user profile
 * - GET /users/test-auth : Test authentication and Gateway headers
 * 
 * ADMIN OPERATIONS:
 * - See AdminUserController for admin user management
 * 
 * AUTHENTICATION:
 * - POST /users: Public endpoint (no authentication)
 * - GET /users/me: Requires USER or ADMIN role
 * - Uses X-User-Id header from API Gateway for authentication
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserServiceInterface userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * CREATE NEW USER ACCOUNT
     * POST /api/users
     * 
     * Public endpoint for user registration.
     * Creates a new user account and sends welcome email.
     * 
     * @param request User registration details (email, name, password)
     * @return Created user with 201 CREATED status
     */
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody RegisterRequestDTO request) {
        // Service throws EmailAllreadyExistsException if email already exists
        UserResponseDTO user = userService.registerUserWithWelcomeEmail(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * GET CURRENT USER PROFILE
     * GET /api/users/me
     * 
     * Returns the authenticated user's profile information.
     * Uses X-User-Id header from API Gateway.
     * 
     * @param userId         Gateway header containing validated user ID
     * @param userEmail      Gateway header containing user email (fallback)
     * @param authentication Spring Security authentication (fallback for internal
     *                       calls)
     * @return Current user profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            Authentication authentication) {

        // GATEWAY APPROACH: Request comes from Gateway (has X-User-Id header)
        if (userId != null) {
            // Gateway has already validated JWT and set userId header
            UserResponseDTO user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        }

        // FALLBACK: Direct request (service-to-service with X-Internal-API-Key)
        // Use SecurityContext Authentication (from ServiceApiKeyFilter or legacy JWT)
        String email = authentication.getName();
        UserResponseDTO user = userService.getCurrentUser(email);
        return ResponseEntity.ok(user);
    }

    /**
     * TEST AUTHENTICATION
     * GET /api/users/test-auth
     * 
     * Development endpoint to verify Gateway headers and authentication.
     * Shows X-User-* headers from Gateway and Spring Security authentication
     * details.
     * 
     * @return Authentication details including Gateway headers and Spring Security
     *         context
     */
    @GetMapping("/test-auth")
    public ResponseEntity<Map<String, Object>> testAuth(
            Authentication authentication,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> authInfo = new HashMap<>();

        // Display Gateway Headers
        Map<String, String> gatewayHeaders = new HashMap<>();
        gatewayHeaders.put("X-User-Id", userId);
        gatewayHeaders.put("X-User-Email", userEmail);
        gatewayHeaders.put("X-User-Role", userRole);
        authInfo.put("gatewayHeaders", gatewayHeaders);

        // Display Spring Security Authentication
        if (authentication == null) {
            authInfo.put("authentication", "No authentication found");
        } else {
            Map<String, Object> authDetails = new HashMap<>();
            authDetails.put("name", authentication.getName());
            authDetails.put("authorities", authentication.getAuthorities().toString());
            authDetails.put("isAuthenticated", authentication.isAuthenticated());
            authInfo.put("authentication", authDetails);
        }

        // Display JWT Claims (should NOT exist if Gateway works correctly)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authInfo.put("WARNING", "Authorization header should NOT be present after Gateway!");
            String token = authHeader.substring(7);
            try {
                Map<String, Object> jwtClaims = new HashMap<>();
                jwtClaims.put("email", jwtUtil.extractEmail(token));
                jwtClaims.put("role", jwtUtil.extractRole(token));
                jwtClaims.put("userId", jwtUtil.extractUserId(token));
                authInfo.put("jwt", jwtClaims);
            } catch (Exception e) {
                authInfo.put("jwt_error", e.getMessage());
            }
        } else {
            authInfo.put("jwt", "No JWT token (CORRECT - Gateway removed it)");
        }

        return ResponseEntity.ok(authInfo);
    }

}
