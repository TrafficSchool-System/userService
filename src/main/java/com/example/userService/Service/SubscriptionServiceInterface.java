package com.example.userService.Service;

import com.example.userService.Dto.CreateSubscriptionRequestDTO;
import com.example.userService.Dto.SubscriptionResponseDTO;
import java.util.List;

public interface SubscriptionServiceInterface {

    /**
     * Skapa en ny subscription (anropas av paymentService eller admin)
     */
    SubscriptionResponseDTO createSubscription(CreateSubscriptionRequestDTO request);

    /**
     * Hämta alla subscriptions för en användare (utan authorization - intern
     * användning)
     */
    List<SubscriptionResponseDTO> getUserSubscriptions(Long userId);

    /**
     * Hämta alla subscriptions för en användare MED authorization check
     */
    List<SubscriptionResponseDTO> getUserSubscriptionsWithAuth(Long requestedUserId, Long authenticatedUserId,
            boolean isAdmin);

    /**
     * Hämta endast aktiva subscriptions för en användare (utan authorization -
     * intern användning)
     */
    List<SubscriptionResponseDTO> getActiveUserSubscriptions(Long userId);

    /**
     * Hämta aktiva subscriptions MED authorization check
     */
    List<SubscriptionResponseDTO> getActiveUserSubscriptionsWithAuth(Long requestedUserId, Long authenticatedUserId,
            boolean isAdmin);

    /**
     * Hämta en specifik subscription via ID (utan authorization - intern
     * användning)
     */
    SubscriptionResponseDTO getSubscriptionById(Long id);

    /**
     * Hämta subscription MED authorization check
     */
    SubscriptionResponseDTO getSubscriptionByIdWithAuth(Long subscriptionId, Long authenticatedUserId, boolean isAdmin);
}