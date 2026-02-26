package com.example.userService.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ==========================================
 * SECURITY CONFIGURATION - REFACTORED
 * ==========================================
 * 
 * MICROSERVICE AUTHENTICATION ARKITEKTUR:
 * 
 * 1. GATEWAY → USERSERVICE (från externa klienter):
 * - Gateway validerar JWT token
 * - Gateway sätter X-User-Id, X-User-Email, X-User-Role headers
 * - GatewayHeaderAuthenticationFilter läser headers och skapar Authentication
 * 
 * 2. SERVICE → USERSERVICE (från andra microservices):
 * - Service skickar X-Internal-API-Key header
 * - ServiceApiKeyFilter validerar API key och skapar Authentication med
 * ROLE_INTERNAL_SERVICE
 * 
 * 3. MAGIC LINK LOGIN:
 * - /api/auth/login - öppet (skickar magic link)
 * - /api/auth/verify-jwt - öppet (validerar token och returnerar JWT)
 * 
 * FILTER ORDNING (VIKTIGT!):
 * 1. ServiceApiKeyFilter (service-to-service först)
 * 2. GatewayHeaderAuthenticationFilter (gateway headers sedan)
 * 
 * VARFÖR DENNA ARKITEKTUR:
 * - Undviker dubblerad JWT-validering i varje service
 * - Gateway är single point of JWT validation
 * - Services litar på Gateway headers
 * - Säkrare: API key för service-to-service
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        private GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;

        @Autowired
        private ServiceApiKeyFilter serviceApiKeyFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http

                                // FÖRKLARING: Stäng av CSRF eftersom vi använder JWT (stateless)
                                .csrf(csrf -> csrf.disable())

                                // FÖRKLARING: Konfigurera vilka endpoints som behöver authentication
                                .authorizeHttpRequests(authz -> authz

                                                // 🟢 ÖPPNA ENDPOINTS - Ingen authentication krävs
                                                .requestMatchers(
                                                                "/api/auth/login", // Begär magic link
                                                                "/api/auth/verify", // Basic verify (utan JWT)
                                                                "/api/auth/verify-jwt", // Verify och få JWT
                                                                "/api/users/register", // Registrera ny användare
                                                                "/api/users/test-auth" // DEBUG: Testa JWT authorities
                                                ).permitAll()

                                                // � USER ENDPOINT - Måste komma FÖRE /api/users/{id} annars matchas
                                                // "me" som id!
                                                .requestMatchers("/api/users/me").hasAnyRole("USER", "ADMIN")

                                                // 🔓 INTERNAL SERVICE-TO-SERVICE ENDPOINTS - För andra microservices
                                                // OCH admins
                                                // VIKTIGT: Dessa endpoints ska ALDRIG nås direkt från externa klienter
                                                // Endast microservices (med API key) eller admins (med JWT) kan komma
                                                // åt dessa
                                                .requestMatchers(
                                                                "/api/users", // GET all users (för AdminService)
                                                                "/api/users/{id}") // GET user by ID (för AdminService)
                                                .hasAnyRole("INTERNAL_SERVICE", "ADMIN")

                                                // 🔐 SUBSCRIPTION CREATE - endast INTERNAL_SERVICE och ADMIN
                                                .requestMatchers("/api/subscriptions")
                                                .hasAnyRole("INTERNAL_SERVICE", "ADMIN")

                                                // 🟡 SUBSCRIPTION READ - USER kan läsa sina egna (ownership-check i
                                                // controller)
                                                .requestMatchers("/api/subscriptions/**").hasAnyRole("USER", "ADMIN")

                                                // 🟦 USER endpoints – kräver ROLE_USER
                                                // OBSERVERA: /api/users/me och /api/users/{id} matchas redan ovan
                                                .requestMatchers("/api/users/**").hasRole("USER")

                                                // 🔒 ADMIN ENDPOINTS - kräver ROLE_ADMIN
                                                .requestMatchers(
                                                                "/api/admin/**")
                                                .hasRole("ADMIN")

                                                // Allt annat blockera
                                                .anyRequest().denyAll())

                                // FÖRKLARING: Stateless sessions - vi använder JWT istället för server sessions
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // FÖRKLARING: Lägg till våra säkerhetsfilter i rätt ordning
                                // 1. ServiceApiKeyFilter kollar först om requesten har en giltig API key
                                // (för service-to-service calls från PaymentService etc.)
                                // 2. GatewayHeaderAuthenticationFilter läser sedan X-User-* headers från
                                // Gateway
                                // (för requests från externa klienter som gått igenom Gateway)
                                .addFilterBefore(serviceApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(gatewayHeaderAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class)

                                // FÖRKLARING: Hantera unauthorized requests
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint((request, response, authException) -> {

                                                        // Returnera 401 Unauthorized med custom meddelande
                                                        response.setStatus(401);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write(
                                                                        "{\"error\": \"Unauthorized\", \"message\": \"Authentication required for this endpoint\"}");
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

                                                        // Logga autentiseringsinformation vid 403
                                                        Authentication auth = SecurityContextHolder.getContext()
                                                                        .getAuthentication();
                                                        logger.error("=== 403 ACCESS DENIED ===");
                                                        logger.error("Request Path: {}", request.getRequestURI());
                                                        logger.error("Request Method: {}", request.getMethod());
                                                        logger.error("Authentication present: {}", auth != null);
                                                        if (auth != null) {
                                                                logger.error("Principal: {}", auth.getPrincipal());
                                                                logger.error("Authorities: {}", auth.getAuthorities());
                                                                logger.error("Is Authenticated: {}",
                                                                                auth.isAuthenticated());
                                                        }
                                                        logger.error("Exception: {}",
                                                                        accessDeniedException.getMessage());
                                                        logger.error("=========================");
                                                        // Returnera 403 Forbidden när användaren saknar rätt roll
                                                        response.setStatus(403);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write(
                                                                        "{\"error\": \"Forbidden\", \"message\": \"Du har inte behörighet att komma åt denna resurs\"}");
                                                }));

                return http.build();
        }
}
