package com.example.userService.Service.email;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService implements EmailServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Override
    public void sendWelcomeEmail(String email, String firstName, String token) {
        log.info("=== WELCOME EMAIL ===");
        log.info("To: {}", email);
        log.info("Hello {}!", firstName);
        log.info("Welcome to Traffic School!");
        log.info("Click here to get started:");
        log.info("http://localhost:5173/?token={}", token);
        log.info("Link is valid for 30 minutes.");
        log.info("====================");
    }

    @Override
    public void sendMagicLinkEmail(String email, String firstName, String token) {
        log.info("=== MAGIC LOGIN LINK ===");
        log.info("To: {}", email);
        log.info("Hello {}!", firstName);
        log.info("Here is your login link:");
        log.info("http://localhost:5173/?token={}", token);
        log.info("Link is valid for 5 minutes.");
        log.info("========================");
    }
}