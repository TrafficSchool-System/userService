package com.example.userService.Service.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.userService.Dto.JwtResponseDTO;
import com.example.userService.Dto.LoginRequestDTO;
import com.example.userService.Dto.UserResponseDTO;
import com.example.userService.Dto.VerifyTokenRequestDTO;
import com.example.userService.Entity.LoginToken;
import com.example.userService.Exception.InvalidTokenException;
import com.example.userService.Exception.UserNotFoundException;
import com.example.userService.Repository.LoginTokenRepository;
import com.example.userService.Security.JwtUtil;
import com.example.userService.Service.email.EmailServiceInterface;
import com.example.userService.Service.user.UserServiceInterface;

import jakarta.transaction.Transactional;

@Service
public class AuthService implements AuthServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private LoginTokenRepository loginTokenRepository;

    @Autowired
    private UserServiceInterface userService; // <- Injicerar interface

    @Autowired
    private EmailServiceInterface emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional // Viktigt för att delete ska fungera
    public String createMagicLink(LoginRequestDTO request) {
        String email = request.getEmail();

        // AFFÄRSLOGIK: Kontrollera om användaren finns
        if (!userService.emailExists(email)) {
            throw new UserNotFoundException("User with email: " + email + " does not exist. Please register first!");

        }

        // Ta bort ALLA gamla oanvända tokens för denna email (enkelt och säkert)
        loginTokenRepository.deleteByEmailAndUsedFalse(email);

        // Hämta användarens namn för personligt email
        UserResponseDTO user = userService.findByEmail(email);

        // Skapa och spara token
        String token = createTokenForEmail(email, 5); // Giltig i 5 min för befintliga användare

        // Skicka länken till konsolen
        emailService.sendMagicLinkEmail(email, user.getFirstName(), token);

        return token;
    }

    @Override
    public UserResponseDTO verifyMagicLink(VerifyTokenRequestDTO request) {
        String tokenString = request.getToken();

        // AFFÄRSLOGIK: Hitta token som inte är använd
        LoginToken loginToken = loginTokenRepository.findByTokenAndUsedFalse(tokenString)
                .orElseThrow(() -> new InvalidTokenException("Invalid or already used link"));

        // AFFÄRSLOGIK: Kontrollera om token har gått ut
        if (LocalDateTime.now().isAfter(loginToken.getExpiresAt())) {
            throw new InvalidTokenException("Link has expired, request a new link");
        }

        // AFFÄRSLOGIK: Kontrollera om token redan används(extra säkerhet)
        if (loginToken.isUsed()) {
            throw new InvalidTokenException("Link has already been used");
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

        // AFFÄRSLOGIK: Generera JWT token med role och userId (BEST PRACTICE)
        String jwtToken = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(), // Konvertera enum till String
                user.getId());

        // AFFÄRSLOGIK: Retunera både token och användarinfo
        return new JwtResponseDTO(jwtToken, user);

    }

    // Används ej. Detta är en util metod. Kan schemalägga detta senare så att den
    // rensar tokens från databasen.
    @Override
    public void cleanupOldTokens() {
        // AFFÄRSLOGIK: Rensa gamla tokens (implementeras senare)
        LocalDateTime now = LocalDateTime.now();

        // Ta bort utgågna tokens
        loginTokenRepository.deleteByExpiresAtBefore(now);

        // Ta bort använda tokens
        loginTokenRepository.deleteByUsedTrue();

        // loginTokenRepository.deleteExpiredAndUsedTokens(now);
        log.info("Old tokens cleaned up at: {}", now);

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
