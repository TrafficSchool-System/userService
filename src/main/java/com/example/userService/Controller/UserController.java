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

import com.example.userService.Service.UserServiceInterface;
import com.example.userService.Security.JwtUtil;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController // Säger till Spring att detta är en REST API
@RequestMapping("/api/users") // Bas URL
public class UserController {

    @Autowired
    private UserServiceInterface userService;

    @Autowired
    private JwtUtil jwtUtil;

    // Registrera en ny användare - Publik endpoint
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody RegisterRequestDTO request) {

        // Service kastar EmailAllreadyExistsException om email redan finns
        UserResponseDTO user = userService.registerUserWithWelcomeEmail(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // Hämta nuvarande användare - Alla inloggade
    // REFACTORED: Läser userId från Gateway header istället för JWT
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            Authentication authentication) {

        // GATEWAY APPROACH: Om request kommer från Gateway (har X-User-Id header)
        if (userId != null) {
            // Gateway har redan validerat JWT och satt userId header
            UserResponseDTO user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        }

        // FALLBACK: Om request kommer direkt (service-to-service med
        // X-Internal-API-Key)
        // Använd SecurityContext Authentication (från ServiceApiKeyFilter eller gamla
        // JWT)
        String email = authentication.getName();
        UserResponseDTO user = userService.getCurrentUser(email);
        return ResponseEntity.ok(user);
    }

    // TEST ENDPOINT - Visa authentication info och Gateway headers
    @GetMapping("/test-auth")
    public ResponseEntity<Map<String, Object>> testAuth(
            Authentication authentication,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> authInfo = new HashMap<>();

        // Visa Gateway Headers
        Map<String, String> gatewayHeaders = new HashMap<>();
        gatewayHeaders.put("X-User-Id", userId);
        gatewayHeaders.put("X-User-Email", userEmail);
        gatewayHeaders.put("X-User-Role", userRole);
        authInfo.put("gatewayHeaders", gatewayHeaders);

        // Visa Spring Security Authentication
        if (authentication == null) {
            authInfo.put("authentication", "No authentication found");
        } else {
            Map<String, Object> authDetails = new HashMap<>();
            authDetails.put("name", authentication.getName());
            authDetails.put("authorities", authentication.getAuthorities().toString());
            authDetails.put("isAuthenticated", authentication.isAuthenticated());
            authInfo.put("authentication", authDetails);
        }

        // Visa JWT Claims (bör INTE finnas om Gateway fungerar korrekt)
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

    /**
     * HÄMTA ALLA ANVÄNDARE
     * GET /api/users
     * 
     * SÄKERHET: Endast ADMIN kan hämta alla användare
     * AdminService använder detta för att aggregera användardata
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * HÄMTA EN ANVÄNDARE VIA ID
     * GET /api/users/{id}
     * 
     * SÄKERHET: Endast ADMIN kan hämta andra användares information
     * AdminService använder detta för att hämta specifik användarinfo
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * UPPDATERA ANVÄNDARINFORMATION
     * PUT /api/users/{id}
     * 
     * SÄKERHET: Endast ADMIN kan uppdatera användarinformation
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDTO updateRequest) {
        UserResponseDTO updatedUser = userService.updateUser(id, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

}
