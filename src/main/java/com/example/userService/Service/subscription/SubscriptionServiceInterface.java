package com.example.userService.Service.subscription;

import com.example.userService.Dto.CreateSubscriptionRequestDTO;
import com.example.userService.Dto.SubscriptionResponseDTO;
import java.util.List;

/**
 * SUBSCRIPTION SERVICE INTERFACE
 * 
 * Hanterar subscriptions för användare i TrafficSchool-systemet.
 * Innehåller både interna metoder (utan authorization) och externa metoder (med
 * authorization).
 * 
 * STRUKTUR:
 * - SKAPA: createSubscription() - Skapar ny subscription från betalning
 * - HÄMTA (INTERN): 3 grundmetoder för intern användning (AdminService,
 * statistik)
 * - HÄMTA (EXTERN): 3 WithAuth-metoder för Controller-lager (frontend-anrop)
 */
public interface SubscriptionServiceInterface {

        // ==========================================
        // SKAPA SUBSCRIPTION - METOD
        // ==========================================

        /**
         * SKAPA NY SUBSCRIPTION
         * 
         * Anropas när en betalning har genomförts (från PaymentService via webhook).
         * Skapar subscription med automatiskt beräknat startDate och endDate.
         * 
         * ANVÄNDS AV: PaymentService webhook, AdminService
         * VALIDERING: Kollar att userId finns och ingen dubblett-subscription för
         * paymentId
         * 
         * @param request DTO med userId, packageId, packageName, validityDays,
         *                validityHours, paymentId
         * @return SubscriptionResponseDTO med skapad subscription
         * @throws UserNotFoundException    om userId inte finns
         * @throws IllegalArgumentException om subscription redan finns för paymentId
         */
        SubscriptionResponseDTO createSubscription(CreateSubscriptionRequestDTO request);

        // ==========================================
        // HÄMTA SUBSCRIPTIONS - GRUNDMETODER
        // ==========================================

        /**
         * HÄMTA ALLA SUBSCRIPTIONS FÖR ANVÄNDARE
         * 
         * OBS: INGEN AUTHORIZATION - Intern metod!
         * Returnerar alla subscriptions (aktiva + inaktiva + utgångna).
         * 
         * ANVÄNDS AV: AdminService, WithAuth-wrapper-metoder
         * ANVÄND INTE DIREKT I CONTROLLER - Använd getUserSubscriptionsWithAuth()
         * istället!
         * 
         * @param userId ID på användare
         * @return Lista med alla subscriptions för användaren
         */
        List<SubscriptionResponseDTO> getUserSubscriptions(Long userId);

        /**
         * HÄMTA ENDAST ICKE-AVBRUTNA SUBSCRIPTIONS
         * 
         * OBS: INGEN AUTHORIZATION - Intern metod!
         * Returnerar subscriptions där cancelled=false (inte manuellt avbrutna).
         * Kan inkludera utgångna subscriptions - måste kolla isExpired() separat!
         * 
         * För att kolla om användaren verkligen har giltigt abonnemang, använd:
         * subscription.isActive() som kollar både !cancelled OCH !isExpired()
         * 
         * I DAGSLÄGET: Alla subscriptions har cancelled=false (tidsbaserade, löper ut
         * automatiskt).
         * FRAMTIDA: Kan användas för att filtrera bort manuellt avbrutna subscriptions.
         * 
         * ANVÄNDS AV: UserService.calculateHasActiveSubscription(), WithAuth-wrapper
         * ANVÄND INTE DIREKT I CONTROLLER - Använd
         * getActiveUserSubscriptionsWithAuth()!
         * 
         * @param userId ID på användare
         * @return Lista med cancelled=false subscriptions
         */
        List<SubscriptionResponseDTO> getActiveUserSubscriptions(Long userId);

        /**
         * HÄMTA SPECIFIK SUBSCRIPTION VIA ID
         * 
         * OBS: INGEN AUTHORIZATION - Intern metod!
         * 
         * ANVÄNDS AV: AdminService, WithAuth-wrapper
         * ANVÄND INTE DIREKT I CONTROLLER - Använd getSubscriptionByIdWithAuth()!
         * 
         * @param id Subscription ID
         * @return SubscriptionResponseDTO
         * @throws IllegalArgumentException om subscription inte finns
         */
        SubscriptionResponseDTO getSubscriptionById(Long id);

        // ==========================================
        // HÄMTA SUBSCRIPTIONS - MED AUTHORIZATION
        // ==========================================

        /**
         * HÄMTA ALLA SUBSCRIPTIONS MED AUTHORIZATION
         * 
         * Validerar att användaren har rätt att se data.
         * REGLER: Admin kan se alla, User kan endast se egna.
         * 
         * ANVÄNDS AV: SubscriptionController
         * ENDPOINT: GET /api/subscriptions/user/{userId}
         * 
         * @param requestedUserId     Vilket userId som begärs
         * @param authenticatedUserId Inloggad användares ID (från JWT)
         * @param isAdmin             Om inloggad användare är admin
         * @return Lista med subscriptions
         * @throws ForbiddenException om user försöker se andra användares data
         */
        List<SubscriptionResponseDTO> getUserSubscriptionsWithAuth(Long requestedUserId, Long authenticatedUserId,
                        boolean isAdmin);

        /**
         * HÄMTA AKTIVA SUBSCRIPTIONS MED AUTHORIZATION
         * 
         * Samma authorization-regler som getUserSubscriptionsWithAuth.
         * 
         * ANVÄNDS AV: SubscriptionController
         * ENDPOINT: GET /api/subscriptions/user/{userId}/active
         * 
         * @param requestedUserId     Vilket userId som begärs
         * @param authenticatedUserId Inloggad användares ID (från JWT)
         * @param isAdmin             Om inloggad användare är admin
         * @return Lista med aktiva subscriptions
         * @throws ForbiddenException om user försöker se andra användares data
         */
        List<SubscriptionResponseDTO> getActiveUserSubscriptionsWithAuth(Long requestedUserId, Long authenticatedUserId,
                        boolean isAdmin);

        /**
         * HÄMTA SPECIFIK SUBSCRIPTION MED AUTHORIZATION
         * 
         * Hämtar först subscription, sedan validerar att användaren har rätt att se
         * den.
         * REGLER: Admin kan se alla, User kan endast se egna.
         * 
         * ANVÄNDS AV: SubscriptionController
         * ENDPOINT: GET /api/subscriptions/{subscriptionId}
         * 
         * @param subscriptionId      ID på subscription
         * @param authenticatedUserId Inloggad användares ID (från JWT)
         * @param isAdmin             Om inloggad användare är admin
         * @return SubscriptionResponseDTO
         * @throws IllegalArgumentException om subscription inte finns
         * @throws ForbiddenException       om user försöker se andras subscription
         */
        SubscriptionResponseDTO getSubscriptionByIdWithAuth(Long subscriptionId, Long authenticatedUserId,
                        boolean isAdmin);
}