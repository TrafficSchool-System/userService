package com.example.userService.Service;

import java.util.List;

import com.example.userService.Dto.RegisterRequestDTO;
import com.example.userService.Dto.UpdateUserRequestDTO;
import com.example.userService.Dto.UserResponseDTO;

public interface UserServiceInterface {
    // Alla klasser som implementerar denna måste ha dessa metoder:

    // Registrera en ny användare i systemet.

    // REQUEST innehåller data från klienten (i detta fall, email, förnamn och
    // efternamn)
    // UserResponseDTO som representerar den registrerade användaren (och inte hela
    // entitetsklassen)
    UserResponseDTO registerUser(RegisterRequestDTO request);

    // Ny metod: Registrera användare och skicka välkomstbrev
    UserResponseDTO registerUserWithWelcomeEmail(RegisterRequestDTO request);

    // Hämtar en användare baserat på email
    // Email den mail addressen som ska sökas efter
    // UserResponseDTO med användarens information. Eller null eception om inget
    // hittas
    UserResponseDTO findByEmail(String email);

    boolean emailExists(String email);

    UserResponseDTO findById(Long id);

    // Hämta inloggad användares data baserat på JWT authentication
    UserResponseDTO getCurrentUser(String email);

    // Hämta alla användare
    List<UserResponseDTO> findAllUsers();

    // Hämta alla användare (alias för findAllUsers)
    List<UserResponseDTO> getAllUsers();

    // Hämta användare via ID (alias för findById)
    UserResponseDTO getUserById(Long id);

    // Uppdatera användare med id
    UserResponseDTO updateUserById(Long id, RegisterRequestDTO request);

    void delteUserById(Long id);

    // Total antal användare
    long countTotalUsers();

    // Uppdatera användarinformation
    UserResponseDTO updateUser(Long id, UpdateUserRequestDTO request);

}
