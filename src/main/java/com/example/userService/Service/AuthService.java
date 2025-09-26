package com.example.userService.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.userService.Dto.JwtResponseDTO;
import com.example.userService.Dto.LoginRequestDTO;
import com.example.userService.Dto.UserResponseDTO;
import com.example.userService.Dto.VerifyTokenRequestDTO;
import com.example.userService.Entity.LoginToken;
import com.example.userService.Exception.InvalidTokenException;
import com.example.userService.Exception.UserNotFoundException;
import com.example.userService.Repository.LoginTokenRepository;
import com.example.userService.Security.JwtUtil;

@Service
public class AuthService implements AuthServiceInterface {

    @Autowired
    private LoginTokenRepository loginTokenRepository;
    
    @Autowired
    private UserServiceInterface userService; // <- Injicerar interface 

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String createMagicLink(LoginRequestDTO request) {
        String email = request.getEmail(); 

        //AFFÄRSLOGIK: Kontrollera om användaren finns
        if (!userService.emailExists(email)) {
            throw new UserNotFoundException("Användaren med email: " + email + " finns inte. Registrera dig först.");
            
        }

        //AFFÄRSLOGIK: Skapa unik Token
        String token = UUID.randomUUID().toString(); 

        //AFFÄRSLOGIK: Token gäller i 5 minuter
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5); 

        //AFFÄRSLOGIK: Skapa LoginToken entity
        LoginToken loginToken = new LoginToken(token, email, expiresAt); 

        //AFFÄRSLOGIK: Spara Token i databasen
        loginTokenRepository.save(loginToken); 

        //AFFÄRSLOGIK: Retuner token (kommer skickas via email senare)
        return token; 
    }

    @Override
    public UserResponseDTO verifyMagicLink(VerifyTokenRequestDTO request) {
        String tokenString = request.getToken(); 

        //AFFÄRSLOGIK: Hitta token som inte är använd
        LoginToken loginToken = loginTokenRepository.findByTokenAndUsedFalse(tokenString)
            .orElseThrow(() -> new InvalidTokenException("Ogiltig eller redan använd token"));

        //AFFÄRSLOGIK: Kontrollera om token har gått ut
        if (LocalDateTime.now().isAfter(loginToken.getExpiresAt())) {
            throw new InvalidTokenException("Token har gått utt, begär en ny magic link");
        }

    
        //AFFÄRSLOGIK: Kontrollera om token redan används(extra säkerhet)
        if (loginToken.isUsed()) {
            throw new InvalidTokenException("Token har redan används.");     
        }

        //AFFÄRSLOGIK: Markera token som använd(One time use)
        loginToken.setUsed(true);
        loginTokenRepository.save(loginToken); 

        //AFFÄRSLOGIK: Hitta användare som ska loggas in
        UserResponseDTO user = userService.findByEmail(loginToken.getEmail());

        //AFFÄRSLOGIK: Retunera användardata (för frontend)
        return user; 
    }

    @Override
    public JwtResponseDTO verifyMagicLinkWithJwt(VerifyTokenRequestDTO request) {
        //AFFÄRSLOGIK: Använder befintlig verifyMagicLink logik
        UserResponseDTO user = verifyMagicLink(request); 

      
            //AFFÄRSLOGIK: Generera JWT token för användare
            String jwtToken = jwtUtil.generateToken(user.getEmail());
            
            //AFFÄRSLOGIK: Retunera både token och användarinfo
            return new JwtResponseDTO(jwtToken, user);        
        
    }

    @Override
    public void cleanupOldTokens() {
        //AFFÄRSLOGIK: Rensa gamla tokens (implementeras senare)
        LocalDateTime now = LocalDateTime.now();

        // loginTokenRepository.deleteExpiredAndUsedTokens(now);
        System.out.println("Städning av gamla tokens - implementeras senare");

    }

   
}
