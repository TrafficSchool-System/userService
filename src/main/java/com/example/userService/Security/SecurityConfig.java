package com.example.userService.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
                                "/api/users/register" // Registrera ny användare
                        ).permitAll()

                        // 🟦 USER endpoints – kräver ROLE_USER
                        .requestMatchers("/api/users/**").hasRole("USER")

                        // 🔒 ADMIN ENDPOINTS - kräver ROLE_ADMIN
                        .requestMatchers(
                                "/api/admin/**").hasRole("ADMIN")

                        // Allt annat blockera
                        .anyRequest().denyAll()
                )

                        

                // FÖRKLARING: Stateless sessions - vi använder JWT istället för server sessions
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // FÖRKLARING: Lägg till vår JWT filter före standard authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // FÖRKLARING: Hantera unauthorized requests
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {

                            // Returnera 401 Unauthorized med custom meddelande
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"error\": \"Unauthorized\", \"message\": \"JWT token krävs för denna endpoint\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {

                            // Returnera 403 Forbidden när användaren saknar rätt roll
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"error\": \"Forbidden\", \"message\": \"Du har inte behörighet att komma åt denna resurs\"}");
                        }));

        return http.build();
    }
}
