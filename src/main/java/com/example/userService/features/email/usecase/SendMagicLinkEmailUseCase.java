package com.example.userService.features.email.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.userService.features.email.service.EmailServiceInterface;
import com.example.userService.features.email.template.MagicLinkEmailTemplate;

/**
 * USE CASE: Send Magic Link Email
 * 
 * Business logic for sending magic link login emails.
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
public class SendMagicLinkEmailUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendMagicLinkEmailUseCase.class);

    private final EmailServiceInterface emailService;
    private final String frontendBaseUrl;

    public SendMagicLinkEmailUseCase(
            EmailServiceInterface emailService,
            @Value("${FRONTEND_URL:http://localhost:5173}") String frontendBaseUrl) {
        this.emailService = emailService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    /**
     * Send magic link login email to user.
     * 
     * @param email     User's email address
     * @param firstName User's first name for personalization
     * @param token     Login token for magic link
     */
    public void execute(String email, String firstName, String token) {
        log.debug("🔑 Preparing magic link email for: {}", email);

        // BUSINESS LOGIC: Construct magic link URL
        String magicLink = frontendBaseUrl + "/?token=" + token;

        // BUSINESS LOGIC: Define email subject
        String subject = "Your Login Link - Traffic School";

        // PRESENTATION: Generate HTML content
        String htmlContent = MagicLinkEmailTemplate.build(firstName, magicLink);

        // INFRASTRUCTURE: Send email
        try {
            emailService.sendEmail(email, subject, htmlContent);
            log.info("✅ Magic link email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send magic link email to: {}", email, e);
            throw new RuntimeException("Failed to send magic link email to " + email, e);
        }
    }
}
