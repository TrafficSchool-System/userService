package com.example.userService.features.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.userService.features.subscription.service.HasActiveSubscriptionUseCase;
import com.example.userService.features.user.dto.UserResponseDTO;
import com.example.userService.features.user.entity.User;
import com.example.userService.features.user.mapper.UserMapper;
import com.example.userService.features.user.repository.UserRepository;
import com.example.userService.shared.exception.UserNotFoundException;

/**
 * USE CASE: Get Single User
 * 
 * Handles retrieval of individual users:
 * 1. Find user by ID
 * 2. Find user by email
 * 3. Get current authenticated user
 * 
 * All methods return UserResponseDTO with active subscription status.
 * This is a dedicated use-case service following vertical slice architecture.
 */
@Service
public class GetUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetUserUseCase.class);

    private final UserRepository userRepository;
    private final HasActiveSubscriptionUseCase hasActiveSubscriptionUseCase;
    private final UserMapper userMapper;

    public GetUserUseCase(
            UserRepository userRepository,
            HasActiveSubscriptionUseCase hasActiveSubscriptionUseCase,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.hasActiveSubscriptionUseCase = hasActiveSubscriptionUseCase;
        this.userMapper = userMapper;
    }

    /**
     * Find user by ID
     * Used by AdminService and internal operations.
     * 
     * @param id User ID to find
     * @return UserResponseDTO with user data and subscription status
     * @throws UserNotFoundException if user doesn't exist
     */
    public UserResponseDTO findById(Long id) {
        log.debug("Finding user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new UserNotFoundException("User not found with ID: " + id);
                });

        boolean hasActiveSubscription = hasActiveSubscriptionUseCase.execute(user.getId());
        UserResponseDTO response = userMapper.toResponse(user, hasActiveSubscription);

        log.debug("User found: {} (ID: {})", user.getEmail(), id);
        return response;
    }

    /**
     * Find user by email
     * Used for login validation and authentication.
     * 
     * @param email Email address to search for
     * @return UserResponseDTO with user data and subscription status
     * @throws UserNotFoundException if user doesn't exist
     */
    public UserResponseDTO findByEmail(String email) {
        log.debug("Finding user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });

        boolean hasActiveSubscription = hasActiveSubscriptionUseCase.execute(user.getId());
        UserResponseDTO response = userMapper.toResponse(user, hasActiveSubscription);

        log.debug("User found by email: {}", email);
        return response;
    }

    /**
     * Get current authenticated user
     * Used by GET /api/users/me endpoint.
     * Returns user profile based on email from JWT token.
     * 
     * @param email Email from authenticated JWT token
     * @return UserResponseDTO with user data and subscription status
     * @throws UserNotFoundException if email is null/empty or user doesn't exist
     */
    public UserResponseDTO getCurrentUser(String email) {
        log.debug("Getting current user for email: {}", email);

        // Validate email parameter
        if (email == null || email.trim().isEmpty()) {
            log.error("Email cannot be null or empty");
            throw new UserNotFoundException("Email kan inte vara null eller tomt");
        }

        // Reuse findByEmail which already handles subscription status
        return findByEmail(email);
    }
}