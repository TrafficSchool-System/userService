package com.example.userService.Service;

import com.example.userService.Dto.RegisterRequestDTO;
import com.example.userService.Dto.UserResponseDTO;

public interface UserServiceInterface {
        //Alla klasser som implementerar denna måste ha dessa metoder: 
        
        //Registrera en ny användare i systemet.

    // REQUEST innehåller data från klienten (i detta fall, email, förnamn och efternamn)
    // UserResponseDTO som representerar den registrerade användaren (och inte hela entitetsklassen)
    UserResponseDTO registerUser(RegisterRequestDTO request);

        //Hämtar en användare baserat på email
    //Email den mail addressen som ska sökas efter
    //UserResponseDTO med användarens information. Eller null eception om inget hittas
    UserResponseDTO findByEmail(String email);  

    boolean emailExists (String email); 

    UserResponseDTO findById(Long id); 
}
