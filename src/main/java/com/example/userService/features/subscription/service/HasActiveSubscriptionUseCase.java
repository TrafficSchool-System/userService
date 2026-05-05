package com.example.userService.features.subscription.service;

import com.example.userService.features.subscription.entity.Subscription;
import com.example.userService.features.subscription.repository.SubscriptionRepository;
import com.example.userService.shared.exception.UserCanNotBeNullException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * HAS ACTIVE SUBSCRIPTION USE CASE
 * 
 * Business logic for checking if a user has any active subscription.
 * 
 * Responsibilities:
 * - Fetch non-cancelled subscriptions for user
 * - Check if any are truly active (not expired)
 * - Return boolean result
 * 
 * Used by: Access control, feature gating, subscription status checks
 */
@Service
public class HasActiveSubscriptionUseCase {

    private static final Logger log = LoggerFactory.getLogger(HasActiveSubscriptionUseCase.class);

    private final SubscriptionRepository subscriptionRepository;

    public HasActiveSubscriptionUseCase(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Execute check for active subscription
     * 
     * @param userId User ID to check
     * @return true if user has at least one active subscription, false otherwise
     */
    public boolean execute(Long userId) {
        if (userId == null) {
            throw new UserCanNotBeNullException("UserId cannot be null");
        }
        log.debug("Checking active subscription for userId={}", userId);

        var subscriptions = subscriptionRepository.findByUserIdAndCancelledFalse(userId);
        log.debug("Found {} non-cancelled subscriptions for userId={}", subscriptions.size(), userId);

        for (Subscription sub : subscriptions) {
            log.debug("Subscription id={} isActive={}", sub.getId(), sub.isActive());
        }

        boolean hasActive = subscriptions.stream().anyMatch(Subscription::isActive);
        log.debug("hasActiveSubscription={} for userId={}", hasActive, userId);

        return hasActive;
    }
}
