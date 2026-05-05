package com.example.userService.features.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.userService.features.email.usecase.SendWelcomeEmailUseCase;
import com.example.userService.features.logintoken.service.CreateLoginTokenUseCase;
import com.example.userService.features.user.dto.CreateUserByAdminDTO;
import com.example.userService.features.user.dto.UserResponseDTO;
import com.example.userService.features.user.entity.User;
import com.example.userService.features.user.mapper.UserMapper;
import com.example.userService.features.user.repository.UserRepository;
import com.example.userService.shared.exception.EmailAllreadyExistsException;
import com.example.userService.shared.exception.PersonalNumberAlreadyExistsException;

/**
 * USE CASE: Create User by Admin
 * 
 * Handles admin-initiated user creation flow:
 * 1. Validates email uniqueness
 * 2. Validates personal number uniqueness
 * 3. Creates new user account
 * 4. Generates welcome token (30 min validity)
 * 5. Sends welcome email with magic link
 * 6. Returns UserResponseDTO with hasActiveSubscription = false
 * 
 * USED BY: AdminService when admin manually creates user + subscription
 * 
 * This is a dedicated use-case service following vertical slice architecture.
 */
@Service
public class CreateUserByAdminUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateUserByAdminUseCase.class);

    private final UserRepository userRepository;
    private final CreateLoginTokenUseCase createLoginTokenUseCase;
    private final SendWelcomeEmailUseCase sendWelcomeEmailUseCase;
    private final UserMapper userMapper;
    private final UserValidationUseCase userValidationService;

    public CreateUserByAdminUseCase(
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
     * Create a new user account (admin operation)
     * User will receive magic link email to set password.
     * 
     * @param request Admin user creation details (email, name, personal number,
     *                phone)
     * @return UserResponseDTO with user data and hasActiveSubscription = false
     * @throws EmailAllreadyExistsException         if email already exists
     * @throws PersonalNumberAlreadyExistsException if personal number already
     *                                              exists
     */
    @Transactional
    public UserResponseDTO create(CreateUserByAdminDTO request) {
        log.info("🔧 Admin creating user: {}", request.getEmail());

        // STEG 1: VALIDERA - Email får inte finnas
        userValidationService.validateEmailNotExists(request.getEmail());

        // STEG 2: VALIDERA - Personnummer får inte finnas
        userValidationService.validatePersonalNumberNotExists(request.getPersonalNumber());

        // STEG 3: SKAPA - Ny användarentitet från admin request
        User user = new User(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPersonalNumber(),
                request.getPhoneNumber());

        // STEG 4: SPARA - Användare i databasen
        User savedUser = userRepository.save(user);
        log.info("✅ User created by admin: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

        // STEG 5: SKAPA - Magic link token (giltig i 30 minuter)
        String token = createLoginTokenUseCase.executeWelcomeToken(savedUser.getEmail());
        log.info("🔑 Magic link token created for: {}", savedUser.getEmail());

        // STEG 6: SKICKA - Magic link email
        sendWelcomeEmailUseCase.execute(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                token);
        log.info("📧 Magic link email sent to: {}", savedUser.getEmail());

        // STEG 7: RETURNERA - DTO med hasActiveSubscription = false (ingen
        // prenumeration än)
        UserResponseDTO response = userMapper.toResponse(savedUser, false);
        log.info("🎉 Admin user creation completed: {}", savedUser.getEmail());

        return response;
    }
}