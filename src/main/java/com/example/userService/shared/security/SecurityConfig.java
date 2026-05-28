package com.example.userService.shared.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ==========================================
 * SECURITY CONFIGURATION - GATEWAY-FIRST ARCHITECTURE
 * ==========================================
 * 
 * MICROSERVICE AUTHENTICATION ARCHITECTURE:
 * 
 * 1. GATEWAY → USERSERVICE (from external clients):
 * - Gateway validates JWT token
 * - Gateway sets X-User-Id, X-User-Email, X-User-Role headers
 * - GatewayHeaderAuthenticationFilter reads headers and creates
 * CustomUserAuthentication
 * - CustomUserAuthentication stored in SecurityContext (SINGLE SOURCE OF TRUTH)
 * 
 * 2. SERVICE → USERSERVICE (from other microservices):
 * - Service sends X-Internal-API-Key header
 * - ServiceApiKeyFilter validates API key and creates CustomUserAuthentication
 * with ROLE_INTERNAL_SERVICE
 * 
 * 3. MAGIC LINK LOGIN:
 * - /api/auth/login - open (sends magic link)
 * - /api/auth/verify - open (validates token and returns JWT)
 * 
 * FILTER ORDER (IMPORTANT!):
 * 1. ServiceApiKeyFilter (service-to-service first)
 * 2. GatewayHeaderAuthenticationFilter (gateway headers second)
 * 
 * SINGLE SOURCE OF TRUTH:
 * - SecurityContext contains CustomUserAuthentication
 * - Controllers use @AuthenticationPrincipal CustomUserAuthentication
 * - NO manual header parsing in controllers
 * - NO request attributes for user identity
 * 
 * AUTHORIZATION:
 * - Role-based: This config + @PreAuthorize
 * - Ownership-based: OwnershipAuthorizer service
 * 
 * WHY THIS ARCHITECTURE:
 * - Avoids duplicate JWT validation in every service
 * - Gateway is single point of JWT validation
 * - Services trust Gateway headers
 * - More secure: API key for service-to-service
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

        @Autowired
        private GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;

        @Autowired
        private ServiceApiKeyFilter serviceApiKeyFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // CSRF disabled for stateless JWT authentication
                                .csrf(csrf -> csrf.disable())

                                // Configure authorization rules
                                .authorizeHttpRequests(authz -> authz
                                                // ========================================
                                                // PUBLIC ENDPOINTS
                                                // ========================================
                                                .requestMatchers(
                                                                "/api/auth/login", // Request magic link
                                                                "/api/auth/verify", // Verify token (basic)
                                                                "/api/auth/tokens", // Verify token and get JWT
                                                                "/api/users/test-auth", // DEBUG endpoint
                                                                "/actuator/health", // Health probe (Azure Container
                                                                                    // Apps)
                                                                "/actuator/info" // Info endpoint
                                                ).permitAll()

                                                // User registration - only POST is public
                                                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users")
                                                .permitAll()

                                                // ========================================
                                                // ADMIN ENDPOINTS
                                                // ========================================
                                                // Accessible by: ADMIN or INTERNAL_SERVICE
                                                .requestMatchers("/api/admin/**")
                                                .hasAnyRole("ADMIN", "INTERNAL_SERVICE")

                                                // ========================================
                                                // USER PROFILE ENDPOINTS
                                                // ========================================
                                                .requestMatchers("/api/users/me")
                                                .hasAnyRole("USER", "ADMIN")

                                                // ========================================
                                                // SUBSCRIPTION ENDPOINTS
                                                // ========================================
                                                // CREATE - INTERNAL_SERVICE and ADMIN only
                                                .requestMatchers(org.springframework.http.HttpMethod.POST,
                                                                "/api/subscriptions")
                                                .hasAnyRole("INTERNAL_SERVICE", "ADMIN")

                                                // READ - USER, ADMIN, and INTERNAL_SERVICE
                                                // Ownership verified in controller
                                                .requestMatchers("/api/subscriptions/**")
                                                .hasAnyRole("USER", "ADMIN", "INTERNAL_SERVICE")

                                                // ========================================
                                                // INTERNAL SERVICE ENDPOINTS
                                                // ========================================
                                                // Only accessible with X-Internal-API-Key
                                                .requestMatchers("/api/internal/**")
                                                .hasRole("INTERNAL_SERVICE")

                                                // Deny all other requests
                                                .anyRequest().denyAll())

                                // Stateless session - using JWT tokens instead of server sessions
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Add security filters in order:
                                // 1. ServiceApiKeyFilter - checks for X-Internal-API-Key header
                                // 2. GatewayHeaderAuthenticationFilter - reads X-User-* headers from Gateway
                                .addFilterBefore(serviceApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(gatewayHeaderAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class)

                                // Exception handling
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(this::handleAuthenticationException)
                                                .accessDeniedHandler(this::handleAccessDeniedException));

                return http.build();
        }

        /**
         * Handle 401 Unauthorized - when authentication is missing
         */
        private void handleAuthenticationException(HttpServletRequest request, HttpServletResponse response,
                        org.springframework.security.core.AuthenticationException authException) throws IOException {
                log.warn("Authentication failed for request: {} {}", request.getMethod(), request.getRequestURI());
                sendJsonError(response, HttpStatus.UNAUTHORIZED, "Unauthorized",
                                "Authentication required for this endpoint");
        }

        /**
         * Handle 403 Forbidden - when user lacks required role/permission
         */
        private void handleAccessDeniedException(HttpServletRequest request, HttpServletResponse response,
                        org.springframework.security.access.AccessDeniedException accessDeniedException)
                        throws IOException {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                log.warn("Access denied: method={} path={} authenticated={} authorities={}",
                                request.getMethod(),
                                request.getRequestURI(),
                                auth != null && auth.isAuthenticated(),
                                auth != null ? auth.getAuthorities() : "none");

                sendJsonError(response, HttpStatus.FORBIDDEN, "Forbidden",
                                "You do not have permission to access this resource");
        }

        /**
         * Send JSON error response
         */
        private void sendJsonError(HttpServletResponse response, HttpStatus status, String error, String message)
                        throws IOException {
                response.setStatus(status.value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(String.format(
                                "{\"error\": \"%s\", \"message\": \"%s\"}",
                                error, message));
        }
}
