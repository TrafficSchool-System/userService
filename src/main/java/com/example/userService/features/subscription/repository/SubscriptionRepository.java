package com.example.userService.features.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.userService.features.subscription.entity.Subscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Hitta alla subscriptions för en användare
    List<Subscription> findByUserId(Long userId);

    // Hitta endast icke-avbrutna subscriptions för en användare
    // OBS: Detta inkluderar även utgångna subscriptions (måste kolla isExpired()
    // separat)
    List<Subscription> findByUserIdAndCancelledFalse(Long userId);

    // Hitta subscription via paymentId (för att undvika dubbletter)
    Optional<Subscription> findByPaymentId(String paymentId);

    // Kolla om användaren har minst en icke-avbruten subscription
    // OBS: Måste kolla isExpired() separat för att verifiera att den är giltig
    boolean existsByUserIdAndCancelledFalse(Long userId);

    /**
     * Hitta alla subscriptions för flera användare (batch loading)
     * Används för att undvika N+1 query problem
     * 
     * @param userIds Lista med user IDs
     * @return Lista med alla subscriptions för dessa användare
     */
    List<Subscription> findByUserIdIn(List<Long> userIds); 

    void deleteByUserId(Long userId);
}