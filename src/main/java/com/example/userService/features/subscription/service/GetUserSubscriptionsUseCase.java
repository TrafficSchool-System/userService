package com.example.userService.features.subscription.service;

import com.example.userService.features.subscription.dto.SubscriptionResponseDTO;
import com.example.userService.features.subscription.repository.SubscriptionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GET USER SUBSCRIPTIONS USE CASE
 * 
 * Business logic for retrieving all subscriptions for a user.
 * Returns both active and expired subscriptions.
 * 
 * Responsibilities:
 * - Fetch all subscriptions for userId
 * - Map to DTOs
 * 
 * Authorization: Handled at controller/adapter layer
 */
@Service
public class GetUserSubscriptionsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetUserSubscriptionsUseCase.class);

    private final SubscriptionRepository subscriptionRepository;

    public GetUserSubscriptionsUseCase(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Execute retrieval of all user subscriptions
     * 
     * @param userId User ID to fetch subscriptions for
     * @return List of all subscriptions (active and expired)
     */
    public List<SubscriptionResponseDTO> execute(Long userId) {
        log.info("Fetching all subscriptions for userId={}", userId);

        return subscriptionRepository.findByUserId(userId)
                .stream()
                .map(SubscriptionResponseDTO::new)
                .collect(Collectors.toList());
    }
}
