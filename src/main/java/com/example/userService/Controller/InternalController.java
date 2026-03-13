package com.example.userService.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userService.Service.user.UserServiceInterface;

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

    @Autowired
    private UserServiceInterface userService;

    /**
     * KOLLA SUBSCRIPTION STATUS
     * GET /api/internal/users/{userId}/subscription-status
     * 
     * Lightweight endpoint för att snabbt kolla om en användare har aktivt
     * abonnemang.
     * Designad för hög performance (endast en boolean, ingen full user data).
     * 
     * ANVÄNDS AV: API Gateway SubscriptionValidationFilter
     * 
     * @param userId User ID att kolla
     * @return { "hasActiveSubscription": true/false }
     */
    @GetMapping("/users/{userId}/subscription-status")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public ResponseEntity<SubscriptionStatusResponse> checkSubscriptionStatus(@PathVariable Long userId) {
        // Hämta user (innehåller hasActiveSubscription efter vår refactoring)
        var user = userService.getUserById(userId);

        return ResponseEntity.ok(new SubscriptionStatusResponse(
                userId,
                user.getHasActiveSubscription()));
    }

    /**
     * DTO för subscription status response
     * Minimal response för optimal performance
     */
    public static class SubscriptionStatusResponse {
        private Long userId;
        private boolean hasActiveSubscription;

        public SubscriptionStatusResponse(Long userId, boolean hasActiveSubscription) {
            this.userId = userId;
            this.hasActiveSubscription = hasActiveSubscription;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public boolean isHasActiveSubscription() {
            return hasActiveSubscription;
        }

        public void setHasActiveSubscription(boolean hasActiveSubscription) {
            this.hasActiveSubscription = hasActiveSubscription;
        }
    }
}
