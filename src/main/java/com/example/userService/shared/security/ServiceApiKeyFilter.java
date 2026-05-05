package com.example.userService.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * ==========================================
 * SERVICE API KEY FILTER
 * ==========================================
 * 
 * This filter runs BEFORE GatewayHeaderAuthenticationFilter and handles
 * authentication for service-to-service communication.
 * 
 * FLOW:
 * 1. Check if request has "X-Internal-API-Key" header
 * 2. If YES and API key is CORRECT:
 * → Create "INTERNAL_SERVICE" authentication
 * → Set in SecurityContext
 * → Request proceeds without JWT
 * 3. If NO or INCORRECT API key:
 * → Do nothing, let GatewayHeaderAuthenticationFilter handle
 * 
 * USAGE:
 * - AdminService calls /api/users with X-Internal-API-Key header
 * - PaymentService calls /api/subscriptions with X-Internal-API-Key header
 * 
 * PRODUCTION:
 * - API Key stored in environment variable (SERVICE_API_KEY)
 * - All services share the SAME key
 * - In Kubernetes: stored as Secret
 * - Rotated regularly
 */
@Component
public class ServiceApiKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ServiceApiKeyFilter.class);

    /**
     * Name of HTTP header containing the API key
     */
    private static final String API_KEY_HEADER = "X-Internal-API-Key";

    /**
     * API key that other services must send
     * Loaded from application.properties (service.api.key)
     * 
     * In production: Store in environment variable or secret manager
     */
    @Value("${service.api.key}")
    private String validApiKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // STEP 1: Get API key from request header
        String apiKey = request.getHeader(API_KEY_HEADER);

        // STEP 2: If API key exists in header
        if (apiKey != null && !apiKey.isEmpty()) {

            log.debug("🔑 API Key detected in request to: {}", request.getRequestURI());

            // STEP 3: Validate API key
            if (apiKey.equals(validApiKey)) {

                log.info("✅ Valid API Key - Service-to-Service authentication successful");
                log.debug("   Request from internal service to: {}", request.getRequestURI());

                // STEP 4: Create authentication for internal services
                // We give it the role "ROLE_INTERNAL_SERVICE"
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"));

                // Use CustomUserAuthentication for consistency
                // Service-to-service authentication uses special constructor (no userId)
                CustomUserAuthentication authentication = new CustomUserAuthentication(
                        "INTERNAL_SERVICE", // Service name (not a user email)
                        authorities // Roles: [ROLE_INTERNAL_SERVICE]
                );

                // STEP 5: Set authentication in SecurityContext
                // Now Spring Security knows this request is authenticated
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("   SecurityContext updated with INTERNAL_SERVICE authentication");

            } else {
                // Invalid API key
                log.warn("⚠️ INVALID API Key detected!");
                log.warn("   Request URI: {}", request.getRequestURI());
                log.warn("   Remote IP: {}", request.getRemoteAddr());

                // Return 401 Unauthorized directly
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\": \"Unauthorized\", \"message\": \"Invalid API Key\"}");
                return; // Stop request here
            }
        } else {
            // No API key in header - this is OK
            // It means this is a regular request with JWT token
            // We let GatewayHeaderAuthenticationFilter handle it
            log.debug("No API Key in request - will check for Gateway headers (JWT)");
        }

        // STEP 6: Continue filter chain
        // If we got here it means either:
        // A) Valid API key (authentication is set)
        // B) No API key (let Gateway header filter handle)
        filterChain.doFilter(request, response);
    }

    /**
     * Log filter configuration at startup
     */
    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        log.info("🔧 ServiceApiKeyFilter initialized");
        log.info("   API Key Header: {}", API_KEY_HEADER);
        log.info("   API Key configured: {}", validApiKey != null ? "YES (hidden)" : "NO");
    }
}
