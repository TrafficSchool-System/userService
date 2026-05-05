package com.example.userService.features.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.userService.features.subscription.service.HasActiveSubscriptionUseCase;
import com.example.userService.features.user.dto.UpdateUserRequestDTO;
import com.example.userService.features.user.dto.UserResponseDTO;
import com.example.userService.features.user.entity.User;
import com.example.userService.features.user.mapper.UserMapper;
import com.example.userService.features.user.repository.UserRepository;
import com.example.userService.shared.exception.EmailAllreadyExistsException;
import com.example.userService.shared.exception.PersonalNumberAlreadyExistsException;
import com.example.userService.shared.exception.UserNotFoundException;

/**
 * USE CASE: Update Existing User
 * 
 * Handles user profile updates with validation:
 * 1. Fetches user by ID
 * 2. Validates email uniqueness (if changed)
 * 3. Validates personal number uniqueness (if changed)
 * 4. Updates only provided fields
 * 5. Saves and returns updated user as DTO
 * 
 * This is a dedicated use-case service following vertical slice architecture.
 */
@Service
public class UpdateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserUseCase.class);

    private final UserRepository userRepository;
    private final UserValidationUseCase userValidationService;
    private final HasActiveSubscriptionUseCase hasActiveSubscriptionUseCase;
    private final UserMapper userMapper;

    public UpdateUserUseCase(
            UserRepository userRepository,
            UserValidationUseCase userValidationService,
            HasActiveSubscriptionUseCase hasActiveSubscriptionUseCase,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userValidationService = userValidationService;
        this.hasActiveSubscriptionUseCase = hasActiveSubscriptionUseCase;
        this.userMapper = userMapper;
    }

    /**
     * Updates an existing user with new information.
     * Supports partial updates - only updates fields provided in the request.
     * 
     * @param id      User ID to update
     * @param request UpdateUserRequestDTO containing new values
     * @return UserResponseDTO with updated user data and subscription status
     * @throws UserNotFoundException                if user with given ID doesn't
     *                                              exist
     * @throws EmailAllreadyExistsException         if new email is already taken
     * @throws PersonalNumberAlreadyExistsException if new personal number is
     *                                              already taken
     */
    @Transactional
    public UserResponseDTO update(Long id, UpdateUserRequestDTO request) {
        log.info("📝 Updating user with ID: {}", id);

        // 1. Fetch user by ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ User not found with ID: {}", id);
                    return new UserNotFoundException("User not found with ID: " + id);
                });

        log.info("✅ User found: {}", user.getEmail());

        // 2. Validate email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            userValidationService.validateEmailNotExists(request.getEmail());
            user.setEmail(request.getEmail());
            log.info("📧 Email updated to: {}", request.getEmail());
        }

        // 3. Validate personal number uniqueness if changed and not empty
        if (request.getPersonalNumber() != null
                && !request.getPersonalNumber().isEmpty()
                && !request.getPersonalNumber().equals(user.getPersonalNumber())) {
            userValidationService.validatePersonalNumberNotExists(request.getPersonalNumber());
            user.setPersonalNumber(request.getPersonalNumber());
            log.info("🆔 Personal number updated");
        }

        // 4. Update other fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
            log.info("👤 First name updated to: {}", request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
            log.info("👤 Last name updated to: {}", request.getLastName());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
            log.info("📱 Phone number updated");
        }

        // 5. Save updated user
        User updatedUser = userRepository.save(user);

        // 6. Convert to DTO with subscription status
        boolean hasActiveSubscription = hasActiveSubscriptionUseCase.execute(updatedUser.getId());
        UserResponseDTO response = userMapper.toResponse(updatedUser, hasActiveSubscription);

        log.info("✅ User successfully updated: {}", updatedUser.getEmail());

        return response;
    }
}