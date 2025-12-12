package com.example.userService.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userService.Dto.RegisterRequestDTO;
import com.example.userService.Dto.UserResponseDTO;
import com.example.userService.Service.UserServiceInterface;

import jakarta.validation.Valid;


@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserServiceInterface userService;

    // Hämta användare med ID - ADMIN ONLY
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {

        // Service kastar UserNotFoundException om användaren inte finns
        UserResponseDTO user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    // Hämta användare med email - ADMIN ONLY
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {

        // Service kastar fel om email inte finns
        UserResponseDTO user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }
    
    // Hämta alla användare
    @GetMapping("/all/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.findAllUsers();
        return ResponseEntity.ok(users); 
    }

    // Uppdatera användare med id
    @PutMapping("/update/{id}")
    public ResponseEntity<UserResponseDTO> updateUser (@PathVariable Long id, @Valid @RequestBody RegisterRequestDTO request) {
        UserResponseDTO updateUser = userService.updateUserById(id, request); 
        return ResponseEntity.ok(updateUser); 
    }

    // Ta bort användare med id
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser (@PathVariable Long id) {
        userService.delteUserById(id);
        return ResponseEntity.noContent().build();
    }

}
