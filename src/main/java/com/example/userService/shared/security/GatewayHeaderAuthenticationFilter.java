package com.example.userService.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * ==========================================
 * GATEWAY HEADER AUTHENTICATION FILTER
 * ==========================================
 * 
 * ARCHITECTURE (Gateway-First Pattern):
 * Client → Gateway (validates JWT) → UserService (reads headers)
 * 
 * PURPOSE:
 * - Converts Gateway headers into Spring Security Authentication
 * - Creates CustomUserAuthentication as SINGLE SOURCE OF TRUTH
 * - Stores authentication in SecurityContextHolder ONLY
 * 
 * FLOW:
 * 1. Gateway validates JWT token
 * 2. Gateway extracts userId, email, role from JWT
 * 3. Gateway sets headers: X-User-Id, X-User-Email, X-User-Role
 * 4. This filter reads headers and creates CustomUserAuthentication
 * 5. Authentication stored in SecurityContext (NOT in request attributes)
 * 
 * SECURITY:
 * - These headers MUST NEVER reach UserService directly from external clients
 * - Gateway MUST strip these headers from incoming requests (security!)
 * - Only Gateway can set these headers
 * 
 * HEADERS:
 * - X-User-Id: Long (userId from JWT)
 * - X-User-Email: String (email from JWT)
 * - X-User-Role: String (USER or ADMIN)
 * 
 * SERVICE-TO-SERVICE:
 * - For internal service calls (PaymentService → UserService)
 * - Use ServiceApiKeyFilter instead (X-Internal-API-Key)
 * 
 * SINGLE SOURCE OF TRUTH:
 * - SecurityContext is the ONLY runtime identity container
 * - NO request attributes for user identity
 * - NO manual header parsing in controllers
 * - Controllers use @AuthenticationPrincipal or SecurityContextHolder
 */
@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayHeaderAuthenticationFilter.class);

    // Header names set by Gateway after JWT validation
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // STEP 1: Read user info from Gateway headers
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        String userEmail = request.getHeader(USER_EMAIL_HEADER);
        String userRole = request.getHeader(USER_ROLE_HEADER);

        // STEP 2: If headers exist (request came from Gateway after JWT validation)
        if (userIdHeader != null && userEmail != null && userRole != null) {

            log.info("🌐 Gateway headers detected - User authenticated by Gateway");
            log.debug("   User ID: {}, Email: {}, Role: {}", userIdHeader, userEmail, userRole);

            try {
                // STEP 3: Parse userId to Long
                Long userId = Long.parseLong(userIdHeader);

                // STEP 4: Create Spring Security authority with ROLE_ prefix (Spring Security
                // requirement)
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userRole.toUpperCase());

                // STEP 5: Create CustomUserAuthentication (SINGLE SOURCE OF TRUTH)
                // This object contains userId, email, and authorities directly accessible
                CustomUserAuthentication authentication = new CustomUserAuthentication(
                        userId, // Direct access via auth.getUserId()
                        userEmail, // Direct access via auth.getEmail()
                        Collections.singletonList(authority) // Roles (e.g., ROLE_USER, ROLE_ADMIN)
                );

                // STEP 6: Store authentication in SecurityContext
                // This is the ONLY place we store user identity
                // NO request attributes, NO duplicate storage
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("✅ CustomUserAuthentication created and set - User: {} (ID: {}), Role: {}",
                        userEmail, userId, userRole);

            } catch (NumberFormatException e) {
                log.error("❌ Invalid userId format in header: {}", userIdHeader);
                // If parsing fails, continue without authentication
                // SecurityConfig will block if endpoint requires auth
            } catch (Exception e) {
                log.error("❌ Failed to create authentication from Gateway headers: {}", e.getMessage());
                // If something goes wrong, continue without authentication
                // SecurityConfig will block if endpoint requires auth
            }
        } else {
            log.debug("⏭️ No Gateway headers found - Request may be public or service-to-service");
        }

        // STEP 7: Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * SECURITY NOTE:
     * This filter MUST run AFTER ServiceApiKeyFilter
     * Because service-to-service calls don't have Gateway headers
     * 
     * Filter order (defined in SecurityConfig):
     * 1. ServiceApiKeyFilter (handles X-Internal-API-Key)
     * 2. GatewayHeaderAuthenticationFilter (handles X-User-* headers)
     * 3. Controllers
     */
}
