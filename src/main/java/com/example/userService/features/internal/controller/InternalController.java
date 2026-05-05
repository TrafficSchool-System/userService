package com.example.userService.features.internal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userService.features.internal.dto.SubscriptionStatusResponse;
import com.example.userService.features.subscription.service.HasActiveSubscriptionUseCase;

/**
 * ==========================================
 * INTERNAL CONTROLLER
 * ==========================================
 * 
 * Endpoints för intern service-to-service kommunikation.
 * Endast microservices med giltig X-Internal-API-Key får anropa dessa.
 * 
 * ANVÄNDS AV:
 * - API Gateway (subscription validation filter)
 * - AdminService (user management)
 * - PaymentService (subscription creation)
 * 
 * SÄKERHET:
 * - @PreAuthorize("hasRole('INTERNAL_SERVICE')")
 * - Kräver X-Internal-API-Key header
 * - Valideras av ServiceApiKeyFilter
 */
@RestController
@RequestMapping("/api/internal")
public class InternalController {

    private final HasActiveSubscriptionUseCase hasActiveSubscriptionUseCase;

    public InternalController(HasActiveSubscriptionUseCase hasActiveSubscriptionUseCase) {
        this.hasActiveSubscriptionUseCase = hasActiveSubscriptionUseCase;
    }

    /**
     * CHECK SUBSCRIPTION STATUS
     * GET /api/internal/users/{userId}/subscription-status
     * 
     * Lightweight endpoint for internal service-to-service communication.
     * Returns only subscription active status (no full user data).
     * 
     * USED BY: API Gateway SubscriptionValidationFilter
     * 
     * @param userId User ID to check
     * @return Subscription status response with userId and active flag
     */
    @GetMapping("/users/{userId}/subscription-status")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public ResponseEntity<SubscriptionStatusResponse> checkSubscriptionStatus(@PathVariable Long userId) {
        boolean active = hasActiveSubscriptionUseCase.execute(userId);
        return ResponseEntity.ok(new SubscriptionStatusResponse(userId, active));
    }
}
