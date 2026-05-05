package com.example.userService.features.subscription.service;

import com.example.userService.features.subscription.dto.SubscriptionResponseDTO;
import com.example.userService.features.subscription.entity.Subscription;
import com.example.userService.features.subscription.repository.SubscriptionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * GET SUBSCRIPTION BY ID USE CASE
 * 
 * Business logic for retrieving a specific subscription by its ID.
 * 
 * Responsibilities:
 * - Fetch subscription by ID
 * - Handle not found case
 * - Map to DTO
 * 
 * Authorization: Handled at controller/adapter layer
 */
@Service
public class GetSubscriptionByIdUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetSubscriptionByIdUseCase.class);

    private final SubscriptionRepository subscriptionRepository;

    public GetSubscriptionByIdUseCase(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Execute retrieval of subscription by ID
     * 
     * @param id Subscription ID
     * @return Subscription as DTO
     * @throws IllegalArgumentException if subscription not found
     */
    public SubscriptionResponseDTO execute(Long id) {
        log.info("Fetching subscription with ID={}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Subscription with ID {} not found", id);
                    return new IllegalArgumentException("Subscription with ID " + id + " not found");
                });

        return new SubscriptionResponseDTO(subscription);
    }
}
