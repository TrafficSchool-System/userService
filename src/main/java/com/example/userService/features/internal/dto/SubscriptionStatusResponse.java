package com.example.userService.features.internal.dto;

/**
 * Subscription Status Response
 * 
 * Minimal DTO for internal service-to-service communication.
 * Used by API Gateway to validate subscription access.
 */
public class SubscriptionStatusResponse {

    private Long userId;
    private boolean active;

    public SubscriptionStatusResponse(Long userId, boolean active) {
        this.userId = userId;
        this.active = active;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
