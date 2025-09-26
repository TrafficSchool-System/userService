package com.example.userService.Service;

import com.example.userService.Dto.JwtResponseDTO;
import com.example.userService.Dto.LoginRequestDTO;
import com.example.userService.Dto.UserResponseDTO;
import com.example.userService.Dto.VerifyTokenRequestDTO;

public interface AuthServiceInterface {

    String createMagicLink(LoginRequestDTO request); 

    UserResponseDTO verifyMagicLink(VerifyTokenRequestDTO request); 

    //Verifera magic link och retunera JWT token
    JwtResponseDTO verifyMagicLinkWithJwt(VerifyTokenRequestDTO request); 
    
    void cleanupOldTokens(); 

}
