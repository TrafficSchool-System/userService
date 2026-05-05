package com.example.userService.features.subscription.service;

import com.example.userService.features.subscription.entity.Subscription;
import com.example.userService.features.subscription.repository.SubscriptionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BATCH HAS ACTIVE SUBSCRIPTIONS USE CASE
 * 
 * Business logic for checking active subscription status for multiple users at
 * once.
 * Optimized for performance - uses single query instead of N queries.
 * 
 * Responsibilities:
 * - Fetch all subscriptions for multiple users in one query
 * - Group by userId
 * - Check if any subscription is active per user
 * - Return map of userId -> hasActive
 * 
 * Performance optimization: Prevents N+1 query problem
 * Used by: Admin panels, bulk user operations, reporting
 */
@Service
public class BatchHasActiveSubscriptionsUseCase {

    private static final Logger log = LoggerFactory.getLogger(BatchHasActiveSubscriptionsUseCase.class);

    private final SubscriptionRepository subscriptionRepository;

    public BatchHasActiveSubscriptionsUseCase(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Execute batch check for active subscriptions
     * 
     * @param userIds List of user IDs to check
     * @return Map of userId -> hasActiveSubscription (false for users with no
     *         subscriptions)
     */
    public Map<Long, Boolean> execute(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        log.debug("Batch checking {} users for active subscriptions", userIds.size());

        // STEP 1: Load all subscriptions in ONE query (performance optimization)
        List<Subscription> allSubscriptions = subscriptionRepository.findByUserIdIn(userIds);

        // STEP 2: Group by userId and check if any subscription is active
        Map<Long, Boolean> resultMap = allSubscriptions.stream()
                .filter(sub -> !sub.isCancelled()) // Only non-cancelled
                .collect(Collectors.groupingBy(
                        Subscription::getUserId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                subs -> subs.stream().anyMatch(Subscription::isActive))));

        // STEP 3: Set false for users without subscriptions
        userIds.forEach(userId -> resultMap.putIfAbsent(userId, false));

        log.debug("Batch checked {} users for active subscriptions", userIds.size());
        return resultMap;
    }
}
