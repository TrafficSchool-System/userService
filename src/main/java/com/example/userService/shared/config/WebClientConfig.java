package com.example.userService.shared.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

/**
 * ==========================================
 * WEBCLIENT CONFIGURATION - USER SERVICE
 * ==========================================
 * Konfigurerar WebClient för service-to-service kommunikation.
 * Används för cascade delete när användare raderas.
 *
 * Använder Azure Container Apps intern DNS direkt (http://payment-service).
 * Ingen @LoadBalanced/Eureka behövs — Azure hanterar lastbalansering.
 * 8s response timeout förhindrar hängande anrop.
 */
@Configuration
public class WebClientConfig {

    @Value("${service.api.key}")
    private String serviceApiKey;

    @Bean
    public WebClient paymentServiceWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(8));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://payment-service")
                .defaultHeader("X-Internal-Source", "user-service")
                .defaultHeader("X-Internal-API-Key", serviceApiKey)
                .build();
    }
}