package com.example.userService.features.email.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * SendGrid Email Service - Pure Infrastructure Adapter
 * 
 * This is a pure infrastructure service - SendGrid API wrapper ONLY.
 * Contains NO business logic, NO email type decisions, NO HTML generation.
 * 
 * Responsibilities:
 * - Send emails via SendGrid API
 * - Handle SendGrid API errors
 * - Configure SendGrid client
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

    private final String sendGridApiKey;
    private final String fromEmail;
    private final String fromName;

    public SendGridEmailService(
            @Value("${sendgrid.api.key}") String sendGridApiKey,
            @Value("${sendgrid.from.email}") String fromEmail,
            @Value("${sendgrid.from.name}") String fromName) {
        this.sendGridApiKey = sendGridApiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    /**
     * Send email via SendGrid.
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
        log.debug("📤 SendGrid: Sending email to: {} with subject: {}", to, subject);

        Email from = new Email(fromEmail, fromName);
        Email toEmail = new Email(to);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.debug("SendGrid response: {}", response.getStatusCode());
            } else {
                log.error("SendGrid API error status={} body={}", response.getStatusCode(), response.getBody());
                throw new RuntimeException(
                        "SendGrid returned status " + response.getStatusCode() + ": " + response.getBody());
            }
        } catch (IOException e) {
            log.error("❌ SendGrid: API error: {}", e.getMessage());
            throw new RuntimeException("Failed to send email via SendGrid", e);
        }
    }
}
