package com.example.userService.features.email.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.userService.features.email.service.EmailServiceInterface;
import com.example.userService.features.email.template.WelcomeEmailTemplate;

/**
 * USE CASE: Send Welcome Email
 * 
 * Business logic for sending welcome emails to new users.
 * Orchestrates email content creation and sending.
 * 
 * Responsibilities:
 * - Construct magic link URL (FRONTEND_URL + token)
 * - Build email subject
 * - Generate HTML content via template
 * - Send email via infrastructure service
 * 
 * This is a vertical slice use-case following clean architecture.
 * No HTTP concerns, pure business orchestration.
 */
@Service
public class SendWelcomeEmailUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendWelcomeEmailUseCase.class);

    private final EmailServiceInterface emailService;
    private final String frontendBaseUrl;

    public SendWelcomeEmailUseCase(
            EmailServiceInterface emailService,
            @Value("${FRONTEND_URL:http://localhost:5173}") String frontendBaseUrl) {
        this.emailService = emailService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    /**
     * Send welcome email to new user.
     * 
     * @param email     User's email address
     * @param firstName User's first name for personalization
     * @param token     Welcome token for magic link
     */
    public void execute(String email, String firstName, String token) {
        log.debug("📧 Preparing welcome email for: {}", email);

        // BUSINESS LOGIC: Construct magic link URL
        String magicLink = frontendBaseUrl + "/?token=" + token;

        // BUSINESS LOGIC: Define email subject
        String subject = "Welcome to Traffic School! 🚗";

        // PRESENTATION: Generate HTML content
        String htmlContent = WelcomeEmailTemplate.build(firstName, magicLink);

        // INFRASTRUCTURE: Send email
        try {
            emailService.sendEmail(email, subject, htmlContent);
            log.info("✅ Welcome email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to: {}", email, e);
            throw new RuntimeException("Failed to send welcome email to " + email, e);
        }
    }
}
