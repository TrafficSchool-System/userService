package com.example.userService.Config;

import com.example.userService.Entity.User;
import com.example.userService.Enum.UserRole;
import com.example.userService.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UserSeeder - Skapar test-användare för utveckling och demo
 * 
 * Körs automatiskt vid applikationens start för att säkerställa
 * att det finns test-användare i systemet.
 * 
 * Dessa användare får automatiskt aktiv prenumeration via PaymentSeeder.
 */
@Configuration
public class UserSeeder {

    private static final Logger logger = LoggerFactory.getLogger(UserSeeder.class);

    @Bean
    CommandLineRunner initTestUsers(UserRepository userRepository) {
        return args -> {
            logger.info("🌱 Checking for test users...");

            // Test User 1 (ID kommer vara 1) - Robert
            createUserIfNotExists(
                    userRepository,
                    "Robert@transportteori.se",
                    "Robert",
                    "Roos",
                    "19900101-1234",
                    "0701234567");

            // Test User 2 (ID kommer vara 2) - Fk
            createUserIfNotExists(
                    userRepository,
                    "Fk@excetra.se",
                    "Fk",
                    "Svensson",
                    "19910202-5678",
                    "0709876543");

            logger.info("✅ Test users initialized - {} users in database", userRepository.count());
        };
    }

    /**
     * Skapar en användare om den inte redan finns
     */
    private void createUserIfNotExists(
            UserRepository userRepository,
            String email,
            String firstName,
            String lastName,
            String personalNumber,
            String phoneNumber) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User(email, firstName, lastName, personalNumber, phoneNumber);
            user.setRole(UserRole.USER);

            User savedUser = userRepository.save(user);
            logger.info("✅ Created test user: {} - ID: {}", email, savedUser.getId());
        } else {
            logger.info("ℹ️ Test user already exists: {}", email);
        }
    }
}
