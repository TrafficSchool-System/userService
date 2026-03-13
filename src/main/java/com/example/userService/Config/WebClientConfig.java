package com.example.userService.Config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * ==========================================
 * WEBCLIENT CONFIGURATION - USER SERVICE
 * ==========================================
 * 
 * Konfigurerar WebClient för service-to-service kommunikation.
 * Används för cascade delete när användare raderas.
 * 
 * FEATURES:
 * - @LoadBalanced: Eureka service discovery automatiskt
 * - Base URLs: Översätts via Eureka (ex: http://payment-service → localhost:8085)
 * - Thread-safe och återanvändbar
 */
@Configuration
public class WebClientConfig {

    /**
     * Load-balanced WebClient Builder
     * Aktiverar automatisk service discovery via Eureka
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * WebClient för PaymentService
     * 
     * ANVÄNDS FÖR:
     * - DELETE /api/admin/payments/users/{userId} (Cascade delete)
     * 
     * Base URL: http://payment-service
     * Resolveras via Eureka till: localhost:8085
     * 
     * SÄKERHET:
     * - X-Internal-API-Key: Autentiserar UserService som intern service
     * - Samma API-nyckel måste användas i alla services
     */
    @Bean
    public WebClient paymentServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("http://payment-service")
                .defaultHeader("X-Internal-Source", "user-service")
                .defaultHeader("X-Internal-API-Key", "TrafficSchool-Internal-Key-2026-CHANGE-IN-PROD")
                .build();
    }
}