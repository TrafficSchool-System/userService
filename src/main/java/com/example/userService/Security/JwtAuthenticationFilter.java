package com.example.userService.Security;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String email = null;
        String jwtToken = null;

        // Steg 1: Hämta token från header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
            try {
                email = jwtUtil.extractEmail(jwtToken);
            } catch (Exception e) {
                logger.warn("JWT token kunde inte parsas: {}", e.getMessage());
            }
        }

        // Steg 2: Validera token och sätt Authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtil.validateToken(jwtToken, email)) {
                    String role = jwtUtil.extractRole(jwtToken);
                    Long userId = jwtUtil.extractUserId(jwtToken);

                    // Lägg till extra attribut för controllers
                    request.setAttribute("userId", userId);
                    request.setAttribute("userEmail", email);

                    if (role != null && !role.isBlank()) {
                        SimpleGrantedAuthority authority =
                                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        email,
                                        null,
                                        Collections.singletonList(authority)
                                );

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        logger.info("JWT authentication successful for user: {} with role: {}", email, role);
                    } else {
                        logger.warn("JWT saknar role-claim för user: {}", email);
                    }
                } else {
                    logger.warn("JWT token validation failed for user: {}", email);
                }
            } catch (Exception e) {
                logger.error("Error during JWT authentication: {}", e.getMessage());
            }
        }

        // Steg 3: Fortsätt kedjan
        filterChain.doFilter(request, response);
    }
}