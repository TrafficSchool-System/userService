package com.example.userService.Controller;

import com.example.userService.Dto.CreateSubscriptionRequestDTO;
import com.example.userService.Dto.SubscriptionResponseDTO;
import com.example.userService.Service.SubscriptionServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

        private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

        @Autowired
        private SubscriptionServiceInterface subscriptionService;

        /**
         * SKAPA SUBSCRIPTION - ENDAST FÖR INTERNAL SERVICES OCH ADMINS
         * POST /api/subscriptions
         * 
         * SÄKERHET: Endast PaymentService (med API key) eller Admin (med JWT) får skapa
         * subscriptions
         * Denna endpoint anropas automatiskt av paymentService när en betalning är PAID
         * 
         * GlobalExceptionHandler hanterar alla exceptions automatiskt
         */
        @PreAuthorize("hasAnyRole('INTERNAL_SERVICE', 'ADMIN')")
        @PostMapping
        public ResponseEntity<SubscriptionResponseDTO> createSubscription(
                        @Valid @RequestBody CreateSubscriptionRequestDTO request) {

                log.info("📥 POST /api/subscriptions - Skapar subscription för userId={}", request.getUserId());

                SubscriptionResponseDTO subscription = subscriptionService.createSubscription(request);
                log.info("✅ Subscription skapad: ID={}", subscription.getId());

                return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
        }

        /**
         * HÄMTA ALLA SUBSCRIPTIONS FÖR EN ANVÄNDARE
         * GET /api/subscriptions/user/{userId}
         * 
         * SÄKERHET: Användare kan bara se SINA EGNA subscriptions (userId från Gateway
         * header)
         * Admin kan se ALLAS subscriptions
         * 
         * REFACTORED: Läser userId och role från Gateway headers
         * 
         * Returnerar alla subscriptions (både aktiva och inaktiva)
         */
        @GetMapping("/user/{userId}")
        public ResponseEntity<List<SubscriptionResponseDTO>> getUserSubscriptions(
                        @PathVariable Long userId,
                        @RequestHeader(value = "X-User-Id", required = false) Long authenticatedUserId,
                        @RequestHeader(value = "X-User-Role", required = false) String userRole,
                        HttpServletRequest request) {

                log.info("📥 GET /api/subscriptions/user/{} - Hämtar alla subscriptions", userId);

                // FALLBACK: Om headers inte finns (service-to-service), använd request
                // attributes
                if (authenticatedUserId == null) {
                        authenticatedUserId = (Long) request.getAttribute("userId");
                }

                // Kolla om användare är ADMIN
                boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);

                log.info("🔒 Authorization check - authenticatedUserId: {}, requestedUserId: {}, isAdmin: {}",
                                authenticatedUserId, userId, isAdmin);

                List<SubscriptionResponseDTO> subscriptions = subscriptionService.getUserSubscriptionsWithAuth(userId,
                                authenticatedUserId, isAdmin);

                log.info("✅ Hittade {} subscriptions för userId={}", subscriptions.size(), userId);

                return ResponseEntity.ok(subscriptions);
        }

        /**
         * HÄMTA AKTIVA SUBSCRIPTIONS FÖR EN ANVÄNDARE
         * GET /api/subscriptions/user/{userId}/active
         * 
         * SÄKERHET: Användare kan bara se SINA EGNA aktiva subscriptions
         * Admin kan se ALLAS aktiva subscriptions
         * 
         * REFACTORED: Läser userId och role från Gateway headers
         * 
         * Returnerar endast aktiva subscriptions
         */
        @GetMapping("/user/{userId}/active")
        public ResponseEntity<List<SubscriptionResponseDTO>> getActiveUserSubscriptions(
                        @PathVariable Long userId,
                        @RequestHeader(value = "X-User-Id", required = false) Long authenticatedUserId,
                        @RequestHeader(value = "X-User-Role", required = false) String userRole,
                        HttpServletRequest request) {

                log.info("📥 GET /api/subscriptions/user/{}/active - Hämtar aktiva subscriptions", userId);

                // FALLBACK: Om headers inte finns, använd request attributes
                if (authenticatedUserId == null) {
                        authenticatedUserId = (Long) request.getAttribute("userId");
                }
                boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);

                List<SubscriptionResponseDTO> activeSubscriptions = subscriptionService
                                .getActiveUserSubscriptionsWithAuth(userId, authenticatedUserId, isAdmin);

                log.info("✅ Hittade {} aktiva subscriptions för userId={}", activeSubscriptions.size(), userId);

                return ResponseEntity.ok(activeSubscriptions);
        }

        /**
         * HÄMTA EN SPECIFIK SUBSCRIPTION
         * GET /api/subscriptions/{id}
         * 
         * SÄKERHET: Användare kan bara se sina egna subscriptions
         * Admin kan se alla subscriptions
         * 
         * REFACTORED: Läser userId och role från Gateway headers
         */
        @GetMapping("/{id}")
        public ResponseEntity<SubscriptionResponseDTO> getSubscriptionById(
                        @PathVariable Long id,
                        @RequestHeader(value = "X-User-Id", required = false) Long authenticatedUserId,
                        @RequestHeader(value = "X-User-Role", required = false) String userRole,
                        HttpServletRequest request) {

                log.info("📥 GET /api/subscriptions/{} - Hämtar subscription", id);

                // FALLBACK: Om headers inte finns, använd request attributes
                if (authenticatedUserId == null) {
                        authenticatedUserId = (Long) request.getAttribute("userId");
                }
                boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);

                SubscriptionResponseDTO subscription = subscriptionService.getSubscriptionByIdWithAuth(id,
                                authenticatedUserId,
                                isAdmin);

                log.info("✅ Subscription hittad: ID={}", subscription.getId());

                return ResponseEntity.ok(subscription);
        }
}