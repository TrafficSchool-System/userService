package com.example.userService.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom Spring Security Authentication object for Gateway-first architecture.
 * 
 * This is the SINGLE SOURCE OF TRUTH for authenticated user identity.
 * 
 * Architecture:
 * - API Gateway validates JWT and sets X-User-* headers
 * - GatewayHeaderAuthenticationFilter reads headers and creates this object
 * - SecurityContextHolder stores this as the authenticated principal
 * - Controllers access via @AuthenticationPrincipal or SecurityContextHolder
 * 
 * DO NOT:
 * - Parse JWT in microservices
 * - Read headers manually in controllers
 * - Use request attributes for identity
 * - Create duplicate identity representations
 */
public class CustomUserAuthentication implements Authentication {

    private final Long userId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private boolean authenticated = true;

    /**
     * Constructor for regular user authentication (from Gateway headers)
     */
    public CustomUserAuthentication(Long userId, String email, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.authorities = authorities;
    }

    /**
     * Constructor for service-to-service authentication
     */
    public CustomUserAuthentication(String serviceName, Collection<? extends GrantedAuthority> authorities) {
        this.userId = null; // No user ID for service accounts
        this.email = serviceName;
        this.authorities = authorities;
    }

    /**
     * Get the user ID directly without parsing headers or attributes
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Get the user email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Check if user has ADMIN role
     */
    public boolean isAdmin() {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Check if this is a service-to-service authentication (not a user)
     */
    public boolean isInternalService() {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_INTERNAL_SERVICE"));
    }

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleWithPrefix));
    }

    // ===== Spring Security Authentication interface methods =====

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null; // No credentials - authentication already done by Gateway
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this; // Return the CustomUserAuthentication itself for @AuthenticationPrincipal
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        this.authenticated = authenticated;
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public String toString() {
        return "CustomUserAuthentication{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", authorities=" + authorities +
                ", authenticated=" + authenticated +
                '}';
    }
}
