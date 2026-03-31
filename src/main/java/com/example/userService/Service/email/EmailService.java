package com.example.userService.Service.email;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Service
public class EmailService implements EmailServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    @Value("${MAGIC_LINK_BASE_URL:http://localhost:5173}")
    private String frontendBaseUrl;

    @Override
    public void sendWelcomeEmail(String toEmail, String firstName, String token) {
        log.info("📧 Sending welcome email to: {}", toEmail);

        String subject = "Welcome to Traffic School! 🚗";
        String htmlContent = buildWelcomeEmailHtml(firstName, token);

        try {
            sendEmail(toEmail, subject, htmlContent);
            log.info("✅ Welcome email sent successfully to: {}", toEmail);
        } catch (IOException e) {
            log.error("❌ Failed to send welcome email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    @Override
    public void sendMagicLinkEmail(String toEmail, String firstName, String token) {
        log.info("🔑 Sending magic login link to: {}", toEmail);

        String subject = "Your Login Link - Traffic School";
        String htmlContent = buildMagicLinkEmailHtml(firstName, token);

        try {
            sendEmail(toEmail, subject, htmlContent);
            log.info("✅ Magic link email sent successfully to: {}", toEmail);
        } catch (IOException e) {
            log.error("❌ Failed to send magic link email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send magic link email", e);
        }
    }

    private void sendEmail(String toEmail, String subject, String htmlContent) throws IOException {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.debug("SendGrid API response: {}", response.getStatusCode());
            } else {
                log.warn("SendGrid API responded with status {}: {}", response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            log.error("SendGrid API error: {}", e.getMessage());
            throw e;
        }
    }

    private String buildWelcomeEmailHtml(String firstName, String token) {
        String magicLink = frontendBaseUrl + "/?token=" + token;

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }
                        .button { display: inline-block; padding: 15px 30px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🚗 Welcome to Traffic School!</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s! 👋</h2>
                            <p>Thank you for registering with Traffic School. We're excited to help you on your journey to getting your driver's license!</p>

                            <p>Click the button below to get started:</p>

                            <a href="%s" class="button">Get Started</a>

                            <p><small>This link is valid for 30 minutes.</small></p>

                            <p>If the button doesn't work, copy and paste this link into your browser:</p>
                            <p style="word-break: break-all; color: #666; font-size: 12px;">%s</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 Traffic School. All rights reserved.</p>
                            <p>If you didn't create this account, please ignore this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(firstName, magicLink, magicLink);
    }

    private String buildMagicLinkEmailHtml(String firstName, String token) {
        String magicLink = frontendBaseUrl + "/?token=" + token;

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }
                        .button { display: inline-block; padding: 15px 30px; background-color: #2196F3; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 10px; margin: 15px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔑 Your Login Link</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s! 👋</h2>
                            <p>You requested a login link for your Traffic School account.</p>

                            <p>Click the button below to log in:</p>

                            <a href="%s" class="button">Log In to Traffic School</a>

                            <div class="warning">
                                <strong>⚠️ Security Notice:</strong>
                                <p>This link is valid for only 5 minutes and can only be used once.</p>
                            </div>

                            <p>If the button doesn't work, copy and paste this link into your browser:</p>
                            <p style="word-break: break-all; color: #666; font-size: 12px;">%s</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 Traffic School. All rights reserved.</p>
                            <p><strong>Didn't request this?</strong> You can safely ignore this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(firstName, magicLink, magicLink);
    }
}