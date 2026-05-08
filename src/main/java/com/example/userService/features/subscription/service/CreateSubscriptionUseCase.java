package com.example.userService.features.subscription.service;

import com.example.userService.features.subscription.dto.CreateSubscriptionRequestDTO;
import com.example.userService.features.subscription.dto.SubscriptionResponseDTO;
import com.example.userService.features.subscription.entity.Subscription;
import com.example.userService.features.subscription.repository.SubscriptionRepository;
import com.example.userService.features.user.repository.UserRepository;
import com.example.userService.shared.exception.SubscriptionAlreadyExistsException;
import com.example.userService.shared.exception.UserNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * CREATE SUBSCRIPTION USE CASE
 * 
 * Business logic for creating a new subscription after payment completion.
 * 
 * Responsibilities:
 * - Validate that user exists
 * - Validate no duplicate payment
 * - Create subscription entity
 * - Persist to database
 * 
 * Called by: PaymentService (via inter-service communication)
 */
@Service
public class CreateSubscriptionUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateSubscriptionUseCase.class);

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public CreateSubscriptionUseCase(
            SubscriptionRepository subscriptionRepository,
            UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Execute subscription creation
     * 
     * @param request Subscription creation request with payment details
     * @return Created subscription as DTO
     * @throws UserNotFoundException    if user does not exist
     * @throws IllegalArgumentException if subscription already exists for payment
     */
    @Transactional
    public SubscriptionResponseDTO execute(CreateSubscriptionRequestDTO request) {
        log.info("Creating subscription for userId={}, packageId={}, paymentId={}",
                request.getUserId(), request.getPackageId(), request.getPaymentId());

        // VALIDATION: User must exist
        if (!userRepository.existsById(request.getUserId())) {
            log.error("User with ID {} does not exist", request.getUserId());
            throw new UserNotFoundException("User with ID " + request.getUserId() + " does not exist");
        }

        // VALIDATION: No duplicate payment
        if (subscriptionRepository.findByPaymentId(request.getPaymentId()).isPresent()) {
            log.warn("Subscription already exists for paymentId={}", request.getPaymentId());
            throw new SubscriptionAlreadyExistsException(request.getPaymentId());
        }

        // EXTEND: If user already has an active subscription, extend it instead of
        // creating a new one
        Optional<Subscription> existingActive = subscriptionRepository
                .findByUserIdAndCancelledFalse(request.getUserId())
                .stream()
                .filter(Subscription::isActive)
                .findFirst();

        if (existingActive.isPresent()) {
            Subscription subscription = existingActive.get();
            subscription.setEndDate(subscription.getEndDate().plusHours(request.getValidityHours()));
            subscription.setPaymentId(request.getPaymentId()); // Update for future deduplication check
            Subscription saved = subscriptionRepository.save(subscription);
            log.info("Subscription extended: ID={}, userId={}, newEndDate={}",
                    saved.getId(), saved.getUserId(), saved.getEndDate());
            return new SubscriptionResponseDTO(saved);
        }

        // CREATE: New subscription from request
        Subscription subscription = new Subscription(
                request.getUserId(),
                request.getPackageId(),
                request.getPackageName(),
                request.getPackagePrice(),
                request.getValidityDays(),
                request.getValidityHours(),
                request.getPaymentId());

        // PERSIST: Save to database
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        log.info("Subscription created: ID={}, userId={}, packageName={}, endDate={}",
                savedSubscription.getId(), savedSubscription.getUserId(),
                savedSubscription.getPackageName(), savedSubscription.getEndDate());

        return new SubscriptionResponseDTO(savedSubscription);
    }
}
