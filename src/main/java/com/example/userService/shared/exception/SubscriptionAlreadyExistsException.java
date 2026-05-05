package com.example.userService.shared.exception;

public class SubscriptionAlreadyExistsException extends RuntimeException {
    public SubscriptionAlreadyExistsException(String paymentId) {
        super("Subscription already exists for paymentId: " + paymentId);
    }
}
