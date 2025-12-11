package com.example.userService.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userService.Dto.RegisterRequestDTO;
import com.example.userService.Dto.UserResponseDTO;

import com.example.userService.Service.UserServiceInterface;

import jakarta.validation.Valid;

@RestController // Säger till Spring att detta är en REST API
@RequestMapping("/api/users") // Bas URL
public class UserController {

    @Autowired
    private UserServiceInterface userService;

    // Registrera en ny användare - Publik endpoint
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody RegisterRequestDTO request) {

        // Service kastar EmailAllreadyExistsException om email redan finns
        UserResponseDTO user = userService.registerUserWithWelcomeEmail(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }


    // Hämta nuvarande användare - Alla inloggade
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponseDTO user = userService.getCurrentUser(email);

        return ResponseEntity.ok(user);
    }

    // TEST ENDPOINT - Visa authorities
    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(Authentication authentication) {
        String authorities = authentication.getAuthorities().toString();
        String name = authentication.getName();
        return ResponseEntity.ok("User: " + name + ", Authorities: " + authorities);
    }

}
