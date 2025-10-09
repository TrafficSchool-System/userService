package com.example.userService.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Tillåt ALLA origins under utveckling
        configuration.addAllowedOriginPattern("*");
        
        // Tillåt alla HTTP-metoder (GET, POST, PUT, DELETE etc)
        configuration.addAllowedMethod("*");
        
        // Tillåt alla headers (Authorization, Content-Type etc)
        configuration.addAllowedHeader("*");
        
        // Tillåt credentials (JWT tokens, cookies etc)
        configuration.setAllowCredentials(true);
        
        // Applicera på alla endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}