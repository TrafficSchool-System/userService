package com.example.userService.features.subscription.service;

import com.example.userService.features.subscription.entity.Subscription;
import com.example.userService.features.subscription.repository.SubscriptionRepository;
import com.example.userService.shared.exception.UserCanNotBeNullException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * DELETE USER SUBSCRIPTIONS USE CASE
 * 
 * Business logic for deleting all subscriptions for a user.
 * Used during cascade user deletion.
 * 
 * Responsibilities:
 * - Find all user subscriptions
 * - Delete them from database
 * - Log deletion count
 * 
 * Called by: DeleteUserService (cascade delete flow)
 */
@Service
public class DeleteUserSubscriptionsUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteUserSubscriptionsUseCase.class);

    private final SubscriptionRepository subscriptionRepository;

    public DeleteUserSubscriptionsUseCase(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Execute deletion of all user subscriptions
     * 
     * @param userId User ID whose subscriptions should be deleted
     */
    @Transactional
    public void execute(Long userId) {
        if (userId == null) {
            throw new UserCanNotBeNullException("userId cannot be null");
        }

        log.debug("Deleting subscriptions for userId={}", userId);

        subscriptionRepository.deleteByUserId(userId);
    }
}
