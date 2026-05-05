package com.example.userService.features.subscription.controller;

import com.example.userService.features.subscription.dto.CreateSubscriptionRequestDTO;
import com.example.userService.features.subscription.dto.SubscriptionResponseDTO;
import com.example.userService.features.subscription.service.*;
import com.example.userService.shared.security.CustomUserAuthentication;
import com.example.userService.shared.security.OwnershipAuthorizer;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================================
 * SUBSCRIPTION CONTROLLER - Clean Architecture (Use-Case Driven)
 * ============================================================================
 * 
 * Handles subscription management endpoints.
 * 
 * CLEAN ARCHITECTURE:
 * - Controller calls use-cases DIRECTLY (no service layer)
 * - Authorization delegated to OwnershipAuthorizer
 * - Zero business logic in controller
 * - Use-cases contain all business logic
 * 
 * AUTHENTICATION PATTERN (Gateway-First):
 * - API Gateway validates JWT and sets X-User-* headers
 * - GatewayHeaderAuthenticationFilter creates CustomUserAuthentication
 * - SecurityContext is the SINGLE SOURCE OF TRUTH
 * - Controllers use @AuthenticationPrincipal (Spring Security standard)
 * - NO manual header parsing
 * - NO request attributes
 * - NO HttpServletRequest dependencies
 * 
 * AUTHORIZATION:
 * - Role-based: @PreAuthorize annotations (Spring Security)
 * - Ownership-based: OwnershipAuthorizer service (centralized)
 * 
 * Uses constructor injection (no @Autowired).
 */
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

        private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

        private final OwnershipAuthorizer ownershipAuthorizer;
        private final CreateSubscriptionUseCase createSubscriptionUseCase;
        private final GetUserSubscriptionsUseCase getUserSubscriptionsUseCase;
        private final GetActiveUserSubscriptionsUseCase getActiveUserSubscriptionsUseCase;
        private final GetSubscriptionByIdUseCase getSubscriptionByIdUseCase;

        public SubscriptionController(
                        OwnershipAuthorizer ownershipAuthorizer,
                        CreateSubscriptionUseCase createSubscriptionUseCase,
                        GetUserSubscriptionsUseCase getUserSubscriptionsUseCase,
                        GetActiveUserSubscriptionsUseCase getActiveUserSubscriptionsUseCase,
                        GetSubscriptionByIdUseCase getSubscriptionByIdUseCase) {
                this.ownershipAuthorizer = ownershipAuthorizer;
                this.createSubscriptionUseCase = createSubscriptionUseCase;
                this.getUserSubscriptionsUseCase = getUserSubscriptionsUseCase;
                this.getActiveUserSubscriptionsUseCase = getActiveUserSubscriptionsUseCase;
                this.getSubscriptionByIdUseCase = getSubscriptionByIdUseCase;
        }

        /**
         * CREATE SUBSCRIPTION - ONLY FOR INTERNAL SERVICES AND ADMINS
         * POST /api/subscriptions
         * 
         * SECURITY: Only PaymentService (with API key) or Admin (with JWT) can create
         * subscriptions
         * This endpoint is called automatically by paymentService when a payment is
         * PAID
         * 
         * GlobalExceptionHandler handles all exceptions automatically
         */
        @PreAuthorize("hasAnyRole('INTERNAL_SERVICE', 'ADMIN')")
        @PostMapping
        public ResponseEntity<SubscriptionResponseDTO> createSubscription(
                        @Valid @RequestBody CreateSubscriptionRequestDTO request) {

                log.info("Creating subscription for userId={}", request.getUserId());

                SubscriptionResponseDTO subscription = createSubscriptionUseCase.execute(request);

                log.info("Subscription created: id={}, userId={}", subscription.getId(), subscription.getUserId());

                return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
        }

        /**
         * GET ALL SUBSCRIPTIONS FOR A USER
         * GET /api/subscriptions/user/{userId}
         * 
         * SECURITY:
         * - User can ONLY see their OWN subscriptions
         * - Admin can see ALL subscriptions
         * - Internal services can see ALL subscriptions
         * 
         * AUTHENTICATION:
         * - Uses @AuthenticationPrincipal to get CustomUserAuthentication from
         * SecurityContext
         * - NO manual header parsing
         * - NO HttpServletRequest dependencies
         * 
         * Returns all subscriptions (both active and inactive)
         */
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'INTERNAL_SERVICE')")
        @GetMapping("/user/{userId}")
        public ResponseEntity<List<SubscriptionResponseDTO>> getUserSubscriptions(
                        @PathVariable Long userId,
                        @AuthenticationPrincipal CustomUserAuthentication auth) {

                log.debug("Fetching subscriptions for userId={}", userId);

                // Delegate ownership check to centralized authorizer
                ownershipAuthorizer.checkUserOwnership(auth, userId);

                // Execute business logic
                List<SubscriptionResponseDTO> subscriptions = getUserSubscriptionsUseCase.execute(userId);

                log.debug("Found {} subscriptions for userId={}", subscriptions.size(), userId);

                return ResponseEntity.ok(subscriptions);
        }

        /**
         * GET ACTIVE SUBSCRIPTIONS FOR A USER
         * GET /api/subscriptions/user/{userId}/active
         * 
         * SECURITY:
         * - User can ONLY see their OWN active subscriptions
         * - Admin can see ALL active subscriptions
         * - Internal services can see ALL active subscriptions
         * 
         * Returns only active subscriptions
         */
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'INTERNAL_SERVICE')")
        @GetMapping("/user/{userId}/active")
        public ResponseEntity<List<SubscriptionResponseDTO>> getActiveUserSubscriptions(
                        @PathVariable Long userId,
                        @AuthenticationPrincipal CustomUserAuthentication auth) {

                log.debug("Fetching active subscriptions for userId={}", userId);

                // Delegate ownership check to centralized authorizer
                ownershipAuthorizer.checkUserOwnership(auth, userId);

                // Execute business logic
                List<SubscriptionResponseDTO> activeSubscriptions = getActiveUserSubscriptionsUseCase.execute(userId);

                log.debug("Found {} active subscriptions for userId={}", activeSubscriptions.size(), userId);

                return ResponseEntity.ok(activeSubscriptions);
        }

        /**
         * GET A SPECIFIC SUBSCRIPTION BY ID
         * GET /api/subscriptions/{id}
         * 
         * SECURITY:
         * - User can ONLY see their own subscriptions
         * - Admin can see all subscriptions
         * - Internal services can see all subscriptions
         */
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'INTERNAL_SERVICE')")
        @GetMapping("/{id}")
        public ResponseEntity<SubscriptionResponseDTO> getSubscriptionById(
                        @PathVariable Long id,
                        @AuthenticationPrincipal CustomUserAuthentication auth) {

                log.debug("Fetching subscription with id={}", id);

                // Fetch subscription
                SubscriptionResponseDTO subscription = getSubscriptionByIdUseCase.execute(id);

                // Check ownership (user can only see their own subscriptions)
                ownershipAuthorizer.checkResourceOwnership(auth, subscription.getUserId(), "subscription");

                log.debug("Subscription retrieved: id={}, userId={}", subscription.getId(), subscription.getUserId());

                return ResponseEntity.ok(subscription);
        }
}