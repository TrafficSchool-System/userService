package com.example.userService.features.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resend Email Service - Pure Infrastructure Adapter
 * 
 * This is a pure infrastructure service - Resend API wrapper ONLY.
 * Contains NO business logic, NO email type decisions, NO HTML generation.
 * 
 * Responsibilities:
 * - Send emails via Resend API
 * - Handle Resend API errors
 * - Configure HTTP client
 * 
 * NOT responsible for:
 * - Building email content (use-case responsibility)
 * - Deciding email types (use-case responsibility)
 * - Constructing magic links (use-case responsibility)
 * - Formatting HTML templates (template responsibility)
 * - Defining email subjects (use-case responsibility)
 * 
 * Clean Architecture: Infrastructure layer only
 */
@Service
public final class SendGridEmailService implements EmailServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailService.class);

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final String resendApiKey;
    private final String fromEmail;
    private final String fromName;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public SendGridEmailService(
            @Value("${resend.api.key:${sendgrid.api.key:}}") String resendApiKey,
            @Value("${resend.from.email:${sendgrid.from.email:noreply@trafficschool.com}}") String fromEmail,
            @Value("${resend.from.name:${sendgrid.from.name:Traffic School}}") String fromName,
            ObjectMapper objectMapper) {
        this.resendApiKey = resendApiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
    * Send email via Resend.
     * 
     * Pure infrastructure operation - delegates to SendGrid API.
     * No business logic, no HTML generation, no content decisions.
     * 
     * @param to          Recipient email address
     * @param subject     Email subject line (defined by use-case)
     * @param htmlContent Complete HTML email content (built by use-case + template)
     */
    @Override
    public void sendEmail(String to, String subject, String htmlContent) {
        log.debug("📤 Resend: Sending email to: {} with subject: {}", to, subject);

        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new RuntimeException("resend.api.key is missing");
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("from", formatFromField());
            payload.put("to", List.of(to));
            payload.put("subject", subject);
            payload.put("html", htmlContent);

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder(URI.create(RESEND_API_URL))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.debug("Resend response: {}", response.statusCode());
            } else {
                log.error("Resend API error status={} body={}", response.statusCode(), response.body());
                throw new RuntimeException(
                        "Resend returned status " + response.statusCode() + ": " + response.body());
            }
        } catch (IOException e) {
            log.error("❌ Resend: API error: {}", e.getMessage());
            throw new RuntimeException("Failed to send email via Resend", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Email sending interrupted", e);
        }
    }

    private String formatFromField() {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new RuntimeException("resend.from.email is missing");
        }

        if (fromEmail.contains("<") && fromEmail.contains(">")) {
            return fromEmail;
        }

        return fromName + " <" + fromEmail + ">";
    }
}
