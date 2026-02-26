package com.example.userService.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
 * Detta filter körs FÖRE JwtAuthenticationFilter och hanterar
 * autentisering för service-to-service kommunikation.
 * 
 * FLOW:
 * 1. Kolla om request har "X-Internal-API-Key" header
 * 2. Om JA och API key är KORREKT:
 * → Skapa en "INTERNAL_SERVICE" authentication
 * → Sätt i SecurityContext
 * → Request går igenom utan JWT
 * 3. Om NEJ eller FELAKTIG API key:
 * → Gör ingenting, låt JwtAuthenticationFilter hantera
 * 
 * ANVÄNDNING:
 * - AdminService anropar /api/users med X-Internal-API-Key header
 * - PaymentService anropar /api/subscriptions med X-Internal-API-Key header
 * 
 * PRODUKTION:
 * - API Key lagras i environment variable (SERVICE_API_KEY)
 * - Alla services delar SAMMA key
 * - I Kubernetes: lagras som Secret
 * - Roteras regelbundet
 */
@Component
public class ServiceApiKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ServiceApiKeyFilter.class);

    /**
     * Namnet på HTTP-headern som innehåller API key
     */
    private static final String API_KEY_HEADER = "X-Internal-API-Key";

    /**
     * API key som andra services måste skicka
     * Laddas från application.properties (service.api.key)
     * 
     * I produktion: Lägg i environment variable eller secret manager
     */
    @Value("${service.api.key}")
    private String validApiKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // STEG 1: Hämta API key från request header
        String apiKey = request.getHeader(API_KEY_HEADER);

        // STEG 2: Om API key finns i headern
        if (apiKey != null && !apiKey.isEmpty()) {

            log.debug("🔑 API Key detected in request to: {}", request.getRequestURI());

            // STEG 3: Validera API key
            if (apiKey.equals(validApiKey)) {

                log.info("✅ Valid API Key - Service-to-Service authentication successful");
                log.debug("   Request from internal service to: {}", request.getRequestURI());

                // STEG 4: Skapa en speciell authentication för interna services
                // Vi ger den rollen "ROLE_INTERNAL_SERVICE"
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "INTERNAL_SERVICE", // Principal (vem)
                        null, // Credentials (lösenord - vi har inget)
                        authorities // Roller
                );

                // STEG 5: Sätt authentication i SecurityContext
                // Nu vet Spring Security att denna request är autentiserad
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("   SecurityContext updated with INTERNAL_SERVICE authentication");

            } else {
                // Felaktig API key
                log.warn("⚠️ INVALID API Key detected!");
                log.warn("   Request URI: {}", request.getRequestURI());
                log.warn("   Remote IP: {}", request.getRemoteAddr());

                // Vi returnerar 401 Unauthorized direkt
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\": \"Unauthorized\", \"message\": \"Invalid API Key\"}");
                return; // Stoppa request här
            }
        } else {
            // Ingen API key i header - detta är OK
            // Det betyder att det är en vanlig request med JWT token
            // Vi låter JwtAuthenticationFilter hantera den
            log.debug("No API Key in request - will check for JWT token");
        }

        // STEG 6: Fortsätt filter-kedjan
        // Om vi kom hit betyder det antingen:
        // A) Valid API key (authentication är satt)
        // B) Ingen API key (låt JWT filter hantera)
        filterChain.doFilter(request, response);
    }

    /**
     * Loggar filtrets konfiguration vid start
     */
    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        log.info("🔧 ServiceApiKeyFilter initialized");
        log.info("   API Key Header: {}", API_KEY_HEADER);
        log.info("   API Key configured: {}", validApiKey != null ? "YES (hidden)" : "NO");
    }
}
