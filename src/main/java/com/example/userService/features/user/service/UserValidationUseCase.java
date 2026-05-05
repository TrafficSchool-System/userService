package com.example.userService.features.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.userService.features.user.repository.UserRepository;
import com.example.userService.shared.exception.EmailAllreadyExistsException;
import com.example.userService.shared.exception.PersonalNumberAlreadyExistsException;

/**
 * DOMAIN SERVICE: User Validation
 * 
 * Provides centralized validation logic for user-related operations.
 * This is a shared service used by multiple use-case services to avoid
 * code duplication and ensure consistent validation rules.
 * 
 * VALIDATIONS:
 * - Email uniqueness checks
 * - Personal number uniqueness checks
 * - Email existence queries
 * - Personal number existence queries
 * 
 * Used by: RegisterUserService, UpdateUserService, CreateUserByAdminService
 */
@Service
public class UserValidationUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserValidationUseCase.class);

    private final UserRepository userRepository;

    public UserValidationUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Validates that an email address doesn't already exist in the database.
     * 
     * @param email Email address to validate
     * @throws EmailAllreadyExistsException if email already exists
     */
    public void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            log.error("Email already exists: {}", email);
            throw new EmailAllreadyExistsException("Email already exists: " + email);
        }
        log.debug("Email validation passed: {}", email);
    }

    /**
     * Validates that a personal number doesn't already exist in the database.
     * Skips validation if personal number is null or empty.
     * 
     * @param personalNumber Personal number to validate
     * @throws PersonalNumberAlreadyExistsException if personal number already exists
     */
    public void validatePersonalNumberNotExists(String personalNumber) {
        // Skip validation if personal number is not provided
        if (personalNumber == null || personalNumber.isEmpty()) {
            log.debug("Personal number validation skipped (empty value)");
            return;
        }

        if (userRepository.existsByPersonalNumber(personalNumber)) {
            log.error("Personal number already exists");
            throw new PersonalNumberAlreadyExistsException("Personal number already exists: " + personalNumber);
        }
        log.debug("Personal number validation passed");
    }

    /**
     * Checks if an email address exists in the database.
     * 
     * @param email Email address to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        boolean exists = userRepository.existsByEmail(email);
        log.debug("Email exists check for '{}': {}", email, exists);
        return exists;
    }

    /**
     * Checks if a personal number exists in the database.
     * 
     * @param personalNumber Personal number to check
     * @return true if personal number exists, false otherwise
     */
    public boolean personalNumberExists(String personalNumber) {
        if (personalNumber == null || personalNumber.isEmpty()) {
            return false;
        }
        boolean exists = userRepository.existsByPersonalNumber(personalNumber);
        log.debug("Personal number exists check: {}", exists);
        return exists;
    }
}