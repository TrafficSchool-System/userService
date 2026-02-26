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
    
    // Hitta endast aktiva subscriptions för en användare
    List<Subscription> findByUserIdAndActiveTrue(Long userId);
    
    // Hitta subscription via paymentId (för att undvika dubbletter)
    Optional<Subscription> findByPaymentId(String paymentId);
    
    // Kolla om användaren redan har en aktiv subscription
    boolean existsByUserIdAndActiveTrue(Long userId);
}