package com.example.userService.features.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.userService.features.logintoken.repository.LoginTokenRepository;
import com.example.userService.features.payment.client.PaymentServiceClient;
import com.example.userService.features.subscription.service.DeleteUserSubscriptionsUseCase;
import com.example.userService.features.user.entity.User;
import com.example.userService.features.user.repository.UserRepository;
import com.example.userService.shared.enums.UserRole;
import com.example.userService.shared.exception.CannotDeleteUserException;
import com.example.userService.shared.exception.UserNotFoundException;

@Service
public class DeleteUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteUserUseCase.class);

    private final UserRepository userRepository;
    private final DeleteUserSubscriptionsUseCase deleteUserSubscriptionsUseCase;
    private final LoginTokenRepository loginTokenRepository;
    private final PaymentServiceClient paymentServiceClient;

    public DeleteUserUseCase(
            UserRepository userRepository,
            DeleteUserSubscriptionsUseCase deleteUserSubscriptionsUseCase,
            LoginTokenRepository loginTokenRepository,
            PaymentServiceClient paymentServiceClient) {
        this.userRepository = userRepository;
        this.deleteUserSubscriptionsUseCase = deleteUserSubscriptionsUseCase;
        this.loginTokenRepository = loginTokenRepository;
        this.paymentServiceClient = paymentServiceClient;
    }

    @Transactional
    public void delete(Long id) {
        log.info("🗑️ Initiating cascade delete for user ID: {}", id);

        // ==========================================
        // 1. FETCH
        // ==========================================
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        // ==========================================
        // 2. BUSINESS RULE
        // ==========================================
        if (user.getRole() == UserRole.ADMIN) {
            throw new CannotDeleteUserException("Cannot delete admin users");
        }

        // ==========================================
        // 3. DELETE CHILD DATA
        // ==========================================
        deleteUserSubscriptionsUseCase.execute(id);
        loginTokenRepository.deleteByEmail(user.getEmail());

        // ==========================================
        // 4. DELETE PAYMENTS (FAIL-SAFE)
        // ==========================================
        try {
            paymentServiceClient.deletePaymentsByUserId(id);
        } catch (Exception e) {
            log.warn("⚠️ Could not delete payments for user ID: {}. Continuing...", id);
        }

        // ==========================================
        // 5. DELETE USER
        // ==========================================
        userRepository.delete(user);

        log.info("✅ User deleted successfully: {}", user.getEmail());
    }
}