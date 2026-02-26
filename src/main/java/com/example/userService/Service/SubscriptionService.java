package com.example.userService.Service;

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

    @Override
    @Transactional
    public SubscriptionResponseDTO createSubscription(CreateSubscriptionRequestDTO request) {
        log.info("📦 Skapar subscription för userId={}, packageId={}, paymentId={}",
                request.getUserId(), request.getPackageId(), request.getPaymentId());

        // Validera att användaren finns
        if (!userRepository.existsById(request.getUserId())) {
            log.error("❌ Användare med ID {} finns inte", request.getUserId());
            throw new UserNotFoundException("Användare med ID " + request.getUserId() + " finns inte");
        }

        // Kontrollera om subscription redan skapats för denna betalning (undvik
        // dubbletter)
        if (subscriptionRepository.findByPaymentId(request.getPaymentId()).isPresent()) {
            log.warn("⚠️ Subscription finns redan för paymentId={}", request.getPaymentId());
            throw new IllegalArgumentException("Subscription finns redan för denna betalning");
        }

        // Skapa subscription
        Subscription subscription = new Subscription(
                request.getUserId(),
                request.getPackageId(),
                request.getPackageName(),
                request.getPackagePrice(),
                request.getValidityDays(),
                request.getValidityHours(),
                request.getPaymentId());

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("✅ Subscription skapad: ID={}, userId={}, packageName={}, endDate={}",
                savedSubscription.getId(), savedSubscription.getUserId(),
                savedSubscription.getPackageName(), savedSubscription.getEndDate());

        return new SubscriptionResponseDTO(savedSubscription);
    }

    @Override
    public List<SubscriptionResponseDTO> getUserSubscriptions(Long userId) {
        log.info("🔍 Hämtar alla subscriptions för userId={}", userId);
        return subscriptionRepository.findByUserId(userId)
                .stream()
                .map(SubscriptionResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionResponseDTO> getActiveUserSubscriptions(Long userId) {
        log.info("🔍 Hämtar aktiva subscriptions för userId={}", userId);
        return subscriptionRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(SubscriptionResponseDTO::new)
                .collect(Collectors.toList());
    }

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
    // AUTHORIZATION METHODS
    // ==========================================

    @Override
    public List<SubscriptionResponseDTO> getUserSubscriptionsWithAuth(
            Long requestedUserId, Long authenticatedUserId, boolean isAdmin) {

        log.info("🔒 Authorization check - requestedUserId: {}, authenticatedUserId: {}, isAdmin: {}",
                requestedUserId, authenticatedUserId, isAdmin);

        // Admin kan se alla användares subscriptions
        if (isAdmin) {
            log.info("✅ Admin access granted");
            return getUserSubscriptions(requestedUserId);
        }

        // Vanlig användare kan ENDAST se sina egna subscriptions
        if (!requestedUserId.equals(authenticatedUserId)) {
            log.warn("⛔ FORBIDDEN: User {} tried to access subscriptions for user {}",
                    authenticatedUserId, requestedUserId);
            throw new ForbiddenException(
                    "Du har inte behörighet att se denna användares subscriptions");
        }

        log.info("✅ User access granted - viewing own subscriptions");
        return getUserSubscriptions(requestedUserId);
    }

    @Override
    public List<SubscriptionResponseDTO> getActiveUserSubscriptionsWithAuth(
            Long requestedUserId, Long authenticatedUserId, boolean isAdmin) {

        log.info("🔒 Authorization check (active) - requestedUserId: {}, authenticatedUserId: {}, isAdmin: {}",
                requestedUserId, authenticatedUserId, isAdmin);

        // Admin kan se alla
        if (isAdmin) {
            log.info("✅ Admin access granted");
            return getActiveUserSubscriptions(requestedUserId);
        }

        // Användare kan endast se sina egna
        if (!requestedUserId.equals(authenticatedUserId)) {
            log.warn("⛔ FORBIDDEN: User {} tried to access active subscriptions for user {}",
                    authenticatedUserId, requestedUserId);
            throw new ForbiddenException(
                    "Du har inte behörighet att se denna användares aktiva subscriptions");
        }

        log.info("✅ User access granted - viewing own active subscriptions");
        return getActiveUserSubscriptions(requestedUserId);
    }

    @Override
    public SubscriptionResponseDTO getSubscriptionByIdWithAuth(
            Long subscriptionId, Long authenticatedUserId, boolean isAdmin) {

        log.info("🔒 Authorization check (by ID) - subscriptionId: {}, authenticatedUserId: {}, isAdmin: {}",
                subscriptionId, authenticatedUserId, isAdmin);

        // Hämta subscription
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> {
                    log.error("❌ Subscription med ID {} finns inte", subscriptionId);
                    return new IllegalArgumentException("Subscription med ID " + subscriptionId + " finns inte");
                });

        // Admin kan se alla
        if (isAdmin) {
            log.info("✅ Admin access granted");
            return new SubscriptionResponseDTO(subscription);
        }

        // Användare kan endast se sina egna
        if (!subscription.getUserId().equals(authenticatedUserId)) {
            log.warn("⛔ FORBIDDEN: User {} tried to access subscription {} owned by user {}",
                    authenticatedUserId, subscriptionId, subscription.getUserId());
            throw new ForbiddenException(
                    "Du har inte behörighet att se denna subscription");
        }

        log.info("✅ User access granted - viewing own subscription");
        return new SubscriptionResponseDTO(subscription);
    }
}