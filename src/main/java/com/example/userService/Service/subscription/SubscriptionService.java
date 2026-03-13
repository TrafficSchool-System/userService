package com.example.userService.Service.subscription;

import com.example.userService.Dto.CreateSubscriptionRequestDTO;
import com.example.userService.Dto.SubscriptionResponseDTO;
import com.example.userService.Entity.Subscription;
import com.example.userService.Repository.SubscriptionRepository;
import com.example.userService.Repository.UserRepository;
import com.example.userService.Exception.UserNotFoundException;
import com.example.userService.Exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionService implements SubscriptionServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    // ==========================================
    // SKAPA SUBSCRIPTION - METOD
    // ==========================================

    /**
     * SKAPA NY SUBSCRIPTION
     * Anropas av PaymentService när betalning är genomförd.
     * Validerar att användaren finns och att subscription inte redan existerar.
     */
    @Override
    @Transactional
    public SubscriptionResponseDTO createSubscription(CreateSubscriptionRequestDTO request) {
        log.info("📦 Skapar subscription för userId={}, packageId={}, paymentId={}",
                request.getUserId(), request.getPackageId(), request.getPaymentId());

        // VALIDERING: Att användaren finns
        if (!userRepository.existsById(request.getUserId())) {
            log.error("❌ Användare med ID {} finns inte", request.getUserId());
            throw new UserNotFoundException("Användare med ID " + request.getUserId() + " finns inte");
        }

        // VALIDERING: Ingen dubblettbetalning (samma paymentId)
        if (subscriptionRepository.findByPaymentId(request.getPaymentId()).isPresent()) {
            log.warn("⚠️ Subscription finns redan för paymentId={}", request.getPaymentId());
            throw new IllegalArgumentException("Subscription finns redan för denna betalning");
        }

        // SKAPA: Ny subscription från request
        Subscription subscription = new Subscription(
                request.getUserId(),
                request.getPackageId(),
                request.getPackageName(),
                request.getPackagePrice(),
                request.getValidityDays(),
                request.getValidityHours(),
                request.getPaymentId());

        // SPARA: I databasen
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        log.info("✅ Subscription skapad: ID={}, userId={}, packageName={}, endDate={}",
                savedSubscription.getId(), savedSubscription.getUserId(),
                savedSubscription.getPackageName(), savedSubscription.getEndDate());

        return new SubscriptionResponseDTO(savedSubscription);
    }

    // ==========================================
    // HÄMTA SUBSCRIPTIONS - GRUNDMETODER
    // ==========================================

    /**
     * HÄMTA ALLA SUBSCRIPTIONS FÖR ANVÄNDARE
     * Intern metod utan authorization check.
     * Används av AdminService och *WithAuth metoder.
     */
    @Override
    public List<SubscriptionResponseDTO> getUserSubscriptions(Long userId) {
        log.info("🔍 Hämtar alla subscriptions för userId={}", userId);
        return subscriptionRepository.findByUserId(userId)
                .stream()
                .map(SubscriptionResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * HÄMTA ENDAST AKTIVA (GILTIGA) SUBSCRIPTIONS
     * Intern metod utan authorization check.
     * 
     * VIKTIGT: Returnerar ENDAST subscriptions som är VERKLIGT aktiva:
     * - cancelled = false (inte manuellt avbruten)
     * - isExpired() = false (inte utgången)
     * 
     * Detta säkerställer att frontend aldrig får utgångna subscriptions när den
     * frågar efter "aktiva" subscriptions.
     * 
     * Om användare har en utgången subscription, returneras tom lista här.
     * Frontend kan då hämta alla subscriptions via getUserSubscriptions() och
     * visa "förnya prenumeration" flödet.
     */
    @Override
    public List<SubscriptionResponseDTO> getActiveUserSubscriptions(Long userId) {
        log.info("🔍 Hämtar aktiva (giltiga) subscriptions för userId={}", userId);
        return subscriptionRepository.findByUserIdAndCancelledFalse(userId)
                .stream()
                .filter(subscription -> subscription.isActive()) // ✅ Filtrera: Endast verkligt aktiva (inte utgångna)
                .map(SubscriptionResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * HÄMTA SPECIFIK SUBSCRIPTION VIA ID
     * Intern metod utan authorization check.
     */
    @Override
    public SubscriptionResponseDTO getSubscriptionById(Long id) {
        log.info("🔍 Hämtar subscription med ID={}", id);
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Subscription med ID {} finns inte", id);
                    return new IllegalArgumentException("Subscription med ID " + id + " finns inte");
                });
        return new SubscriptionResponseDTO(subscription);
    }

    // ==========================================
    // HÄMTA SUBSCRIPTIONS - MED AUTHORIZATION
    // ==========================================

    /**
     * HÄMTA ALLA SUBSCRIPTIONS MED AUTHORIZATION
     * Används av Controller-lager.
     * Validerar att användaren har rätt att se data.
     * 
     * REGLER:
     * - Admin kan se ALLA användares subscriptions
     * - User kan ENDAST se EGNA subscriptions
     */
    @Override
    public List<SubscriptionResponseDTO> getUserSubscriptionsWithAuth(
            Long requestedUserId, Long authenticatedUserId, boolean isAdmin) {

        log.info("🔒 Authorization check - requestedUserId: {}, authenticatedUserId: {}, isAdmin: {}",
                requestedUserId, authenticatedUserId, isAdmin);

        // AUTHORIZATION: Kolla om användaren har rätt
        checkAuthorization(requestedUserId, authenticatedUserId, isAdmin, "subscriptions");

        // ÅterAnvänding: Anropa grundmetod
        return getUserSubscriptions(requestedUserId);
    }

    /**
     * HÄMTA AKTIVA SUBSCRIPTIONS MED AUTHORIZATION
     * Samma authorization-regler som getUserSubscriptionsWithAuth.
     */
    @Override
    public List<SubscriptionResponseDTO> getActiveUserSubscriptionsWithAuth(
            Long requestedUserId, Long authenticatedUserId, boolean isAdmin) {

        log.info("🔒 Authorization check (active) - requestedUserId: {}, authenticatedUserId: {}, isAdmin: {}",
                requestedUserId, authenticatedUserId, isAdmin);

        // AUTHORIZATION: Kolla om användaren har rätt
        checkAuthorization(requestedUserId, authenticatedUserId, isAdmin, "aktiva subscriptions");

        // ÅTERANVÄND: Anropa grundmetod
        return getActiveUserSubscriptions(requestedUserId);
    }

    /**
     * HÄMTA SPECIFIK SUBSCRIPTION MED AUTHORIZATION
     * 
     * SPECIAL FALL: Här måste vi först hämta subscription för att se vem som äger
     * den.
     * Därför kan vi inte använda checkAuthorization() direkt.
     */
    @Override
    public SubscriptionResponseDTO getSubscriptionByIdWithAuth(
            Long subscriptionId, Long authenticatedUserId, boolean isAdmin) {

        log.info("🔒 Authorization check (by ID) - subscriptionId: {}, authenticatedUserId: {}, isAdmin: {}",
                subscriptionId, authenticatedUserId, isAdmin);

        // HÄMTA: Subscription först
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> {
                    log.error("❌ Subscription med ID {} finns inte", subscriptionId);
                    return new IllegalArgumentException("Subscription med ID " + subscriptionId + " finns inte");
                });

        // AUTHORIZATION: Admin kan se alla
        if (isAdmin) {
            log.info("✅ Admin access granted");
            return new SubscriptionResponseDTO(subscription);
        }

        // AUTHORIZATION: User kan endast se sina egna
        if (!subscription.getUserId().equals(authenticatedUserId)) {
            log.warn("⛔ FORBIDDEN: User {} tried to access subscription {} owned by user {}",
                    authenticatedUserId, subscriptionId, subscription.getUserId());
            throw new ForbiddenException(
                    "Du har inte behörighet att se denna subscription");
        }

        log.info("✅ User access granted - viewing own subscription");
        return new SubscriptionResponseDTO(subscription);
    }

    // ==========================================
    // PRIVAT HJÄLPMETOD - AUTHORIZATION CHECK
    // ==========================================

    /**
     * VALIDERA AUTHORIZATION
     * 
     * Centraliserad logik för att kolla om en användare har rätt att se data.
     * Används av getUserSubscriptionsWithAuth och
     * getActiveUserSubscriptionsWithAuth.
     * 
     * REGLER:
     * - Admin (isAdmin=true): Får se ALLT ✅
     * - User (isAdmin=false): Får ENDAST se EGNA data (requestedUserId ==
     * authenticatedUserId) ✅
     * - User som försöker se ANDRAS data: FORBIDDEN ❌
     * 
     * @param requestedUserId     - Vilket userId som data begärs för
     * @param authenticatedUserId - Vilket userId som är inloggat
     * @param isAdmin             - Om inloggad användare är admin
     * @param resourceName        - Namnet på resursen (för loggning)
     * @throws ForbiddenException om användaren inte har behörighet
     */
    private void checkAuthorization(Long requestedUserId, Long authenticatedUserId, boolean isAdmin,
            String resourceName) {
        // ADMIN: Tillåt allt
        if (isAdmin) {
            log.info("✅ Admin access granted");
            return;
        }

        // USER: Endast egna data
        if (!requestedUserId.equals(authenticatedUserId)) {
            log.warn("⛔ FORBIDDEN: User {} tried to access {} for user {}",
                    authenticatedUserId, resourceName, requestedUserId);
            throw new ForbiddenException(
                    "Du har inte behörighet att se denna användares " + resourceName);
        }

        log.info("✅ User access granted - viewing own {}", resourceName);
    }
}