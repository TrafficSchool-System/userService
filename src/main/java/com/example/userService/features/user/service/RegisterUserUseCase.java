package com.example.userService.features.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.userService.features.email.usecase.SendWelcomeEmailUseCase;
import com.example.userService.features.logintoken.service.CreateLoginTokenUseCase;
import com.example.userService.features.user.dto.RegisterRequestDTO;
import com.example.userService.features.user.dto.UserResponseDTO;
import com.example.userService.features.user.entity.User;
import com.example.userService.features.user.mapper.UserMapper;
import com.example.userService.features.user.repository.UserRepository;
import com.example.userService.shared.exception.EmailAllreadyExistsException;
import com.example.userService.shared.exception.PersonalNumberAlreadyExistsException;

/**
 * USE CASE: Register New User
 * 
 * Handles the complete user registration flow:
 * 1. Validates email uniqueness
 * 2. Validates personal number uniqueness
 * 3. Creates new user account
 * 4. Generates welcome token
 * 5. Sends welcome email with login link
 * 
 * This is a dedicated use-case service following vertical slice architecture.
 */
@Service
public class RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);

    private final UserRepository userRepository;
    private final CreateLoginTokenUseCase createLoginTokenUseCase;
    private final SendWelcomeEmailUseCase sendWelcomeEmailUseCase;
    private final UserMapper userMapper;
    private final UserValidationUseCase userValidationService;

    public RegisterUserUseCase(
            UserRepository userRepository,
            CreateLoginTokenUseCase createLoginTokenUseCase,
            SendWelcomeEmailUseCase sendWelcomeEmailUseCase,
            UserMapper userMapper,
            UserValidationUseCase userValidationService) {
        this.userRepository = userRepository;
        this.createLoginTokenUseCase = createLoginTokenUseCase;
        this.sendWelcomeEmailUseCase = sendWelcomeEmailUseCase;
        this.userMapper = userMapper;
        this.userValidationService = userValidationService;
    }

    /**
     * Register a new user with welcome email
     * 
     * @param request Registration data (email, name, personal number, phone)
     * @return UserResponseDTO with user data and subscription status
     * @throws EmailAllreadyExistsException         if email already exists
     * @throws PersonalNumberAlreadyExistsException if personal number already
     *                                              exists
     */
    @Transactional
    public UserResponseDTO register(RegisterRequestDTO request) {
        log.debug("Processing new user registration");

        // STEG 1: VALIDERA - Email får inte finnas
        userValidationService.validateEmailNotExists(request.getEmail());

        // STEG 2: VALIDERA - Personnummer får inte finnas
        userValidationService.validatePersonalNumberNotExists(request.getPersonalNumber());

        // STEG 3: SKAPA - Ny användarentitet
        User user = new User(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPersonalNumber(),
                request.getPhoneNumber());

        // STEG 4: SPARA - Användare i databasen
        User savedUser = userRepository.save(user);
        log.debug("User persisted to database with id={}", savedUser.getId());

        // STEG 5: SKAPA - Välkomsttoken (giltig i 30 minuter)
        String token = createLoginTokenUseCase.executeWelcomeToken(savedUser.getEmail());
        log.debug("Welcome token created for userId={}", savedUser.getId());

        // STEG 6: SKICKA - Välkomstemail med magic link
        sendWelcomeEmailUseCase.execute(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                token);
        log.debug("Welcome email dispatched for userId={}", savedUser.getId());

        // STEG 7: RETURNERA - DTO med användardata
        UserResponseDTO response = userMapper.toResponse(savedUser, false);
        log.info("User registration completed userId={}", savedUser.getId());

        return response;
    }

}
