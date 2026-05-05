package com.example.userService.features.subscription.service;

import com.example.userService.features.subscription.dto.SubscriptionResponseDTO;
import com.example.userService.features.subscription.repository.SubscriptionRepository;
import com.example.userService.shared.exception.UserNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GET ACTIVE USER SUBSCRIPTIONS USE CASE
 * 
 * Business logic for retrieving only ACTIVE (valid) subscriptions for a user.
 * 
 * A subscription is considered active when:
 * - cancelled = false (not manually cancelled)
 * - isExpired() = false (not past end date)
 * 
 * This ensures frontend never receives expired subscriptions when requesting
 * "active" subscriptions. If all subscriptions are expired, returns empty list.
 * Frontend can then show "renew subscription" flow.
 * 
 * Authorization: Handled at controller/adapter layer
 */
@Service
public class GetActiveUserSubscriptionsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetActiveUserSubscriptionsUseCase.class);

    private final SubscriptionRepository subscriptionRepository;

    public GetActiveUserSubscriptionsUseCase(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Execute retrieval of active user subscriptions
     * 
     * @param userId User ID to fetch active subscriptions for
     * @return List of only active (non-cancelled, non-expired) subscriptions
     */
    public List<SubscriptionResponseDTO> execute(Long userId) {

        if (userId == null) {
            throw new UserNotFoundException("User can not be null");
        }

        log.debug("Fetching active (valid) subscriptions for userId={}", userId);
        return subscriptionRepository.findByUserIdAndCancelledFalse(userId)
                .stream()
                .filter(subscription -> subscription.isActive()) // Filter: Only truly active (not expired)
                .map(SubscriptionResponseDTO::new)
                .collect(Collectors.toList());
    }
}
