package com.example.userService.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userService.Dto.RegisterRequestDTO;
import com.example.userService.Dto.UserResponseDTO;
import com.example.userService.Service.AuthServiceInterface;
import com.example.userService.Service.EmailServiceInterface;
import com.example.userService.Service.UserServiceInterface;

import jakarta.validation.Valid;

@RestController // Säger till Spring att detta är en REST API
@RequestMapping("/api/users") // Bas URL
public class UserController {

    @Autowired
    private UserServiceInterface userService;

    @Autowired
    private AuthServiceInterface authService;

    @Autowired
    private EmailServiceInterface emailService;
    
    // Registrera en ny användare
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody RegisterRequestDTO request){
        
        //Service kastar EmailAllreadyExistsException om email redan finns
        UserResponseDTO user = userService.registerUserWithWelcomeEmail(request); 

        return ResponseEntity.status(HttpStatus.CREATED).body(user); 
    }

    //Hämta användare med ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@Valid @PathVariable Long id){
        
        //Service kastar UserNotFoundException om användaren inte finns
        UserResponseDTO user = userService.findById(id); 

        return ResponseEntity.ok(user); 
    }

    //Hämta användare med email (för admin)
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@Valid @PathVariable String email){

        //Service kastar fel om email inte finns 
        UserResponseDTO user = userService.findByEmail(email); 

        return ResponseEntity.ok(user); 
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName(); 
        UserResponseDTO user = userService.getCurrentUser(email); 

        return ResponseEntity.ok(user); 
    }

    

}
