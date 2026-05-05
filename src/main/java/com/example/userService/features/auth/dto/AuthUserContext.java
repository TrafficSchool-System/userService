package com.example.userService.features.auth.dto;

/**
 * Auth User Context
 * 
 * Minimal authentication context containing only essential user identity data.
 * Used exclusively within the Auth domain for token generation and
 * verification.
 * 
 * This decouples Auth layer from User domain (UserResponseDTO).
 * Auth layer should not depend on full user entity details.
 * 
 * UPDATED: Now includes hasActiveSubscription for frontend routing logic.
 * Frontend needs this to determine if user should see Paywall or Dashboard.
 * 
 * @param userId                User's unique identifier
 * @param email                 User's email address (JWT subject)
 * @param role                  User's role (e.g., USER, ADMIN)
 * @param hasActiveSubscription Whether user has an active subscription
 */
public record AuthUserContext(
                Long userId,
                String email,
                String role,
                boolean hasActiveSubscription) {
}
