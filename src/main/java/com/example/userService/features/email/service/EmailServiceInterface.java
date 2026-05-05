package com.example.userService.features.email.service;

/**
 * Email Service Interface - Pure Infrastructure Contract
 * 
 * This is a pure infrastructure interface.
 * Contains NO business logic, NO email type decisions, NO HTML generation.
 * 
 * Responsibilities:
 * - Send emails via external service (SendGrid)
 * 
 * NOT responsible for:
 * - Building email content (use-case responsibility)
 * - Deciding email types (use-case responsibility)
 * - Constructing magic links (use-case responsibility)
 * 
 * Clean Architecture: Infrastructure layer only
 */
public interface EmailServiceInterface {

    /**
     * Send email via external email service.
     * 
     * Pure infrastructure operation - no business logic.
     * 
     * @param to          Recipient email address
     * @param subject     Email subject line
     * @param htmlContent Complete HTML email content
     */
    void sendEmail(String to, String subject, String htmlContent);

}
