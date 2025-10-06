package com.example.userService.Service;

import org.springframework.stereotype.Service;

@Service
public class EmailService implements EmailServiceInterface {

    @Override
    public void sendWelcomeEmail(String email, String firstName, String token) {
        System.out.println("=== VÄLKOMSTBREV ===");
        System.out.println("Till: " + email);
        System.out.println("Hej " + firstName + "!");
        System.out.println("Välkommen till Trafikskolan!");
        System.out.println("Klicka här för att komma igång:");
        System.out.println("http://localhost:5173/?token=" + token);
        System.out.println("Länken är giltig i 30 minuter.");
        System.out.println("====================");
    }

    @Override
    public void sendMagicLinkEmail(String email, String firstName, String token) {
        System.out.println("=== INLOGGNINGSLÄNK ===");
        System.out.println("Till: " + email);
        System.out.println("Hej " + firstName + "!");
        System.out.println("Här är din inloggningslänk:");
        System.out.println("http://localhost:5173/?token=" + token);
        System.out.println("Länken är giltig i 5 minuter.");
        System.out.println("========================");
    }
}