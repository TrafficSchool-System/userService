package com.example.userService.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userService.Dto.UserResponseDTO;
import com.example.userService.Service.UserServiceInterface;

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

}
