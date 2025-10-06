package com.example.userService.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userService.Dto.JwtResponseDTO;
import com.example.userService.Dto.LoginRequestDTO;
import com.example.userService.Dto.UserResponseDTO;
import com.example.userService.Dto.VerifyTokenRequestDTO;
import com.example.userService.Service.AuthServiceInterface;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthServiceInterface authService;

    // Skicka magic link via email
    @PostMapping("/login")
    public ResponseEntity<String> sendMagicLink(@Valid @RequestBody LoginRequestDTO request) {

        //Service kastar UserNotFoundException om användare inte finns
        authService.createMagicLink(request); 

        return ResponseEntity.ok("Länk skickat till: " + request.getEmail()); 
    }

    // Verifiera magic link token
    @PostMapping("/verify")
    public ResponseEntity<UserResponseDTO> verifyMagicLink(@Valid @RequestBody VerifyTokenRequestDTO request) {

        // Service kastar UserNotFoundException om användare inte finns
        UserResponseDTO user = authService.verifyMagicLink(request);

        return ResponseEntity.ok(user);
    }

    //Verifera magic link och få JWT token 
    @PostMapping("/verify-jwt")
    public ResponseEntity<JwtResponseDTO> verifyMagicLinkWithJwt(@Valid @RequestBody VerifyTokenRequestDTO request){

        // Service kastar exceptions om något går fel
        JwtResponseDTO jwtResponse = authService.verifyMagicLinkWithJwt(request); 

        return ResponseEntity.ok(jwtResponse);

    }
}