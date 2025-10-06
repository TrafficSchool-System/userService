package com.example.userService.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.userService.Dto.RegisterRequestDTO;
import com.example.userService.Dto.UserResponseDTO;
import com.example.userService.Entity.User;
import com.example.userService.Entity.LoginToken;
import com.example.userService.Exception.EmailAllreadyExistsException;
import com.example.userService.Exception.UserNotFoundException;
import com.example.userService.Repository.UserRepository;
import com.example.userService.Repository.LoginTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService implements UserServiceInterface {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginTokenRepository loginTokenRepository;

    @Autowired
    private EmailServiceInterface emailService;

    // Grundläggande registrering - används av registerUserWithWelcomeEmail()
    @Override
    public UserResponseDTO registerUser(RegisterRequestDTO request) {
        // AFFÄRSLOGIK: Komtrollera om email redan finns
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAllreadyExistsException("Email finns redan registrerad: " + request.getEmail());
        }

        // AFFÄRSLOGIK: Skapa en ny användare från request
        User user = new User(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName());

        // AFFÄRSLOGIK: Spara användaren i databasen
        User savedUser = userRepository.save(user);

        // AFFÄRSLOGIK: Konvertera till UserResponseDTO och retunera
        return new UserResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO registerUserWithWelcomeEmail(RegisterRequestDTO request) {
        // AFFÄRSLOGIK: Registrera användaren först
        UserResponseDTO user = registerUser(request);

        // AFFÄRSLOGIK: Skapa välkomsttoken (30 minuter giltighetstid)
        String token = createWelcomeToken(user.getEmail());

        // AFFÄRSLOGIK: Skicka välkomstbrev
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName(), token);

        return user;
    }

    // PRIVAT HJÄLPMETOD: Skapar welcome token för nya användare
    private String createWelcomeToken(String email) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        LoginToken loginToken = new LoginToken(token, email, expiresAt);
        loginTokenRepository.save(loginToken);

        return token;
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        // AFFÄRSLOGIK: Hitta användare i databasen med email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Användare hittades inte med: " + email));

        // AFFÄRSLOGIK: Retunera null om användaren inte finns
        return new UserResponseDTO(user);
    }

    @Override
    public boolean emailExists(String email) {
        // AFFÄRSLOGIK: Enkelt check om email finns
        return userRepository.existsByEmail(email);

    }

    @Override
    public UserResponseDTO findById(Long id) {
        // AFFÄRSLOGIK: Hitta användare med ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Användaren hittades ej med id: " + id));

        // AFFÄRSLOGIK: Retunera användaren om den finns
        return new UserResponseDTO(user);

    }

}
