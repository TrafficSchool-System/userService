package com.example.userService.features.payment.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.userService.shared.exception.PaymentServiceException;

/**
 * Client för att kommunicera med PaymentService
 * Abstrahera Webclient-anrop från userService
*/
@Service
public class PaymentServiceClient {

    private final Logger log = LoggerFactory.getLogger(PaymentServiceClient.class); 

    private final WebClient paymentServicWebClient;

    @Autowired
    public PaymentServiceClient (WebClient paymentServiceWebClient) {
        this.paymentServicWebClient = paymentServiceWebClient;
    }

    /**
     * Raderar alla payments för en specifik användare
     * Anropas vid cascade delete användare
     * @param userId - User ID vars payments ska raderas
     * @throws RuntimeException om anropet misslyckas
     */
    public void deletePaymentsByUserId(Long userId) {
        log.info("Calling PaymentService to delete payments for user ID: {}", userId);

        try {
            paymentServicWebClient
                .delete()
                .uri("/api/admin/payments/users/{id}", userId)
                .retrieve()
                .toBodilessEntity()
                .block();

            log.info("Succesfully deleted payments for user ID: {}", userId);
        } catch(Exception e) {
            log.error("Failed to delete payments for user ID: {}", userId, e);
            throw new PaymentServiceException("Could not delete payments in PaymentService for user ID: " + userId, e); 
        }
    }

}
