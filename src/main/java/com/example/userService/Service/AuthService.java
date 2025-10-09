package com.example.userService.Service;

import java.time.LocalDateTime;
import java.util.List;
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
    private EmailServiceInterface emmailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String createMagicLink(LoginRequestDTO request) {
        String email = request.getEmail();

        // AFFÄRSLOGIK: Kontrollera om användaren finns
        if (!userService.emailExists(email)) {
            throw new UserNotFoundException("Användaren med email: " + email + " finns inte. Registrera dig först!");

        }

        // Hitta alla aktiva tokens för denna mail
        List<LoginToken> activeTokens = loginTokenRepository.findByEmailAndUsedFalse(email);

        // Räkna tokens från senaste 15 minuterna (enkel rate limitng)
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        long recentTokens = activeTokens.stream()
                .filter(token -> token.getCreatedAt().isAfter(fifteenMinutesAgo))
                .count();

        if (recentTokens >= 3) {
            throw new InvalidTokenException("För många inloggningsförsök. Försök igen om 15 minuter.");
        }

        // Ta bort ALLA gamla tokens (enkelt och effektivt)
        if (!activeTokens.isEmpty()) {
            loginTokenRepository.deleteByEmailAndUsedFalse(email);
        }

        // Hämta användarens namn för personligt email
        UserResponseDTO user = userService.findByEmail(email);

        // Skapa och spara token
        String token = createTokenForEmail(email, 5); // Giltig i 5 min för befintliga användare

        // Skicka länken till konsolen
        emmailService.sendMagicLinkEmail(email, user.getFirstName(), token);

        return token;
    }

    @Override
    public UserResponseDTO verifyMagicLink(VerifyTokenRequestDTO request) {
        String tokenString = request.getToken();

        // AFFÄRSLOGIK: Hitta token som inte är använd
        LoginToken loginToken = loginTokenRepository.findByTokenAndUsedFalse(tokenString)
                .orElseThrow(() -> new InvalidTokenException("Ogiltig eller redan använd Länk"));

        // AFFÄRSLOGIK: Kontrollera om token har gått ut
        if (LocalDateTime.now().isAfter(loginToken.getExpiresAt())) {
            throw new InvalidTokenException("Länk har gått utt, begär en ny länk");
        }

        // AFFÄRSLOGIK: Kontrollera om token redan används(extra säkerhet)
        if (loginToken.isUsed()) {
            throw new InvalidTokenException("Länk har redan används.");
        }

        // AFFÄRSLOGIK: Markera token som använd(One time use)
        loginToken.setUsed(true);
        loginTokenRepository.save(loginToken);

        // AFFÄRSLOGIK: Hitta användare som ska loggas in
        UserResponseDTO user = userService.findByEmail(loginToken.getEmail());

        // AFFÄRSLOGIK: Retunera användardata (för frontend)
        return user;
    }

    @Override
    public JwtResponseDTO verifyMagicLinkWithJwt(VerifyTokenRequestDTO request) {
        // AFFÄRSLOGIK: Använder befintlig verifyMagicLink logik
        UserResponseDTO user = verifyMagicLink(request);

        // AFFÄRSLOGIK: Generera JWT token för användare
        String jwtToken = jwtUtil.generateToken(user.getEmail());

        // AFFÄRSLOGIK: Retunera både token och användarinfo
        return new JwtResponseDTO(jwtToken, user);

    }

    @Override
    public void cleanupOldTokens() {
        // AFFÄRSLOGIK: Rensa gamla tokens (implementeras senare)
        LocalDateTime now = LocalDateTime.now();

        // loginTokenRepository.deleteExpiredAndUsedTokens(now);
        System.out.println("Städning av gamla tokens - implementeras senare");

    }

    // PRIVAT HJÄLPMETOD: Skapar token för magic links (5 minuter)
    private String createTokenForEmail(String email, int minutesValid) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(minutesValid);
        LocalDateTime createdAt = LocalDateTime.now();

        LoginToken loginToken = new LoginToken(token, email, expiresAt, createdAt);
        loginTokenRepository.save(loginToken);

        return token;
    }

}
