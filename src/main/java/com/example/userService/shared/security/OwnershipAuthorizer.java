package com.example.userService.shared.security;

import com.example.userService.shared.exception.ForbiddenException;
import org.springframework.stereotype.Component;

/**
 * Centralized ownership-based authorization service.
 * 
 * Architecture:
 * - Role-based authorization: Handled by Spring Security (@PreAuthorize,
 * SecurityConfig)
 * - Ownership-based authorization: Handled by this service
 * 
 * Purpose:
 * - Prevent code duplication across controllers
 * - Provide consistent authorization behavior
 * - Make authorization logic testable in isolation
 * - Keep controllers thin (delegation pattern)
 * 
 * Usage in controllers:
 * 
 * <pre>
 * {@code
 * &#64;GetMapping("/user/{userId}/subscriptions")
 * public ResponseEntity<?> getUserSubscriptions(
 *         &#64;PathVariable Long userId,
 *         @AuthenticationPrincipal CustomUserAuthentication auth) {
 *     
 *     // Delegate ownership check to this service
 *     ownershipAuthorizer.checkUserOwnership(auth, userId);
 *     
 *     // If we reach here, authorization passed
 *     return ResponseEntity.ok(useCase.execute(userId));
 * }
 * }
 * </pre>
 */
@Component
public class OwnershipAuthorizer {

    /**
     * Verify that authenticated user can access data belonging to the specified
     * userId.
     * 
     * Rules:
     * - Admins can access any user's data (bypass)
     * - Internal services can access any user's data (bypass)
     * - Regular users can ONLY access their own data
     * 
     * @param auth            The authenticated user (from SecurityContext)
     * @param requestedUserId The userId being accessed in the request
     * @throws ForbiddenException if user cannot access the requested userId
     */
    public void checkUserOwnership(CustomUserAuthentication auth, Long requestedUserId) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("Authentication required to access this resource");
        }

        // Admin bypass: Admins can access any user's data
        if (auth.isAdmin()) {
            return;
        }

        // Internal service bypass: Service-to-service calls can access any user's data
        if (auth.isInternalService()) {
            return;
        }

        // Regular user: Can ONLY access their own data
        if (auth.getUserId() == null) {
            throw new ForbiddenException("User ID not available in authentication context");
        }

        if (!auth.getUserId().equals(requestedUserId)) {
            throw new ForbiddenException(
                    String.format("Access denied. You can only access your own data. " +
                            "Requested userId: %d, Your userId: %d",
                            requestedUserId, auth.getUserId()));
        }
    }

    /**
     * Verify that authenticated user owns a specific resource.
     * 
     * This is similar to checkUserOwnership but works with resource ownership.
     * For example, checking if user owns a specific subscription, payment, etc.
     * 
     * Rules:
     * - Admins can access any resource (bypass)
     * - Internal services can access any resource (bypass)
     * - Regular users can ONLY access resources they own
     * 
     * @param auth            The authenticated user (from SecurityContext)
     * @param resourceOwnerId The userId that owns the resource
     * @param resourceType    Human-readable resource type (for error messages)
     * @throws ForbiddenException if user does not own the resource
     */
    public void checkResourceOwnership(CustomUserAuthentication auth, Long resourceOwnerId, String resourceType) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("Authentication required to access this resource");
        }

        // Admin bypass
        if (auth.isAdmin()) {
            return;
        }

        // Internal service bypass
        if (auth.isInternalService()) {
            return;
        }

        // Regular user ownership check
        if (auth.getUserId() == null) {
            throw new ForbiddenException("User ID not available in authentication context");
        }

        if (!auth.getUserId().equals(resourceOwnerId)) {
            throw new ForbiddenException(
                    String.format("Access denied. You do not own this %s. " +
                            "Resource owner: %d, Your userId: %d",
                            resourceType, resourceOwnerId, auth.getUserId()));
        }
    }

    /**
     * Verify that authenticated user is an admin.
     * 
     * Use this for operations that require admin privileges.
     * NOTE: Consider using @PreAuthorize("hasRole('ADMIN')") instead in most cases.
     * 
     * @param auth The authenticated user (from SecurityContext)
     * @throws ForbiddenException if user is not an admin
     */
    public void requireAdmin(CustomUserAuthentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("Authentication required");
        }

        if (!auth.isAdmin()) {
            throw new ForbiddenException("Admin privileges required for this operation");
        }
    }

    /**
     * Verify that authenticated user is an internal service.
     * 
     * Use this for service-to-service only endpoints.
     * NOTE: Consider using @PreAuthorize("hasRole('INTERNAL_SERVICE')") instead.
     * 
     * @param auth The authenticated user (from SecurityContext)
     * @throws ForbiddenException if caller is not an internal service
     */
    public void requireInternalService(CustomUserAuthentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("Authentication required");
        }

        if (!auth.isInternalService()) {
            throw new ForbiddenException("This endpoint is only accessible to internal services");
        }
    }

    /**
     * Check if authenticated user can perform an action.
     * 
     * This is a flexible method for custom authorization logic.
     * 
     * @param auth             The authenticated user
     * @param canPerformAction Boolean condition that must be true
     * @param errorMessage     Error message if authorization fails
     * @throws ForbiddenException if authorization fails
     */
    public void require(CustomUserAuthentication auth, boolean canPerformAction, String errorMessage) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("Authentication required");
        }

        if (!canPerformAction) {
            throw new ForbiddenException(errorMessage);
        }
    }
}
