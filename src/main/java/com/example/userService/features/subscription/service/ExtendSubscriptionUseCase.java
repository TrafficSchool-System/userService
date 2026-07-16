package com.example.userService.features.subscription.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.userService.features.subscription.dto.ExtendSubscriptionRequestDTO;
import com.example.userService.features.subscription.dto.SubscriptionResponseDTO;
import com.example.userService.features.subscription.entity.Subscription;
import com.example.userService.features.subscription.repository.SubscriptionRepository;
import com.example.userService.shared.exception.UserNotFoundException;

@Service
public class ExtendSubscriptionUseCase {

    /**
     * EXTEND SUBSCRIPTION USE CASE
     * 
     * Förlänger en elevs aktiva prenumiration med X antal dagar.
     * Anropas av adminService via internal API.
     * 
     * LOGIK:
     * - Hittar elevens aktiva subscription
     * - Om subscription är utgången: förlängs från dagens datum
     * - Om subscription är aktiv: förlängs från nuvarande slutdatum
     * - Uppdaterar subscription i databasen
     */

    private static final Logger log = LoggerFactory.getLogger(ExtendSubscriptionUseCase.class);

    private final SubscriptionRepository subscriptionRepository;

    public ExtendSubscriptionUseCase(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public SubscriptionResponseDTO execute(ExtendSubscriptionRequestDTO request) {
        log.info("Extending subscription for userID={} by {} days", request.getUserId(), request.getDays());

        // Hämta senaste ice-avbrutna subscription för användaren
        List<Subscription> subscriptions = subscriptionRepository.findByUserIdAndCancelledFalse(request.getUserId());

        if (subscriptions.isEmpty()) {
            throw new UserNotFoundException(
                "No subscription found for userId=" + request.getUserId());
        }

        // Välj den senaste högst ID
        Subscription subscription = subscriptions.stream()
            .max(java.util.Comparator.comparingLong(Subscription::getId))
            .get(); 

        // Beräkna nytt end endDate
        LocalDateTime newEndDate; 
        if (subscription.isExpired()) {
            // Utgången: börja från nu
            newEndDate = LocalDateTime.now(ZoneOffset.UTC)
                .plusDays(request.getDays()); 
            log.info("Subscription was expired — extending from now: newEndDate={}", newEndDate); 
        } else {
            // Aktiv: lägg till dagar på befintligt slutdatum´
            newEndDate = subscription.getEndDate().plusDays(request.getDays()); 
            log.info("Subscription is active - extending from endDate: newEndDate={}", newEndDate);
        }

        subscription.setEndDate(newEndDate);
        Subscription saved = subscriptionRepository.save(subscription); 

        log.info("Subscription extended: id={}, userId={}, newEndDate={}", saved.getId(), saved.getUserId(), saved.getEndDate());

        return new SubscriptionResponseDTO(saved);
    }

    
}
