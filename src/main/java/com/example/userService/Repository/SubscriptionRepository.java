package com.example.userService.Repository;

import com.example.userService.Entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}