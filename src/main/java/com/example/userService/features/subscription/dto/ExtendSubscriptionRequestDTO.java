package com.example.userService.features.subscription.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ExtendSubscriptionRequestDTO {

    @NotNull(message = "UserId is required")
    private Long userId;
    
    @NotNull(message = "Day is required")
    @Min(value = 1, message = "Day must be greater than or equal to 1")
    private Integer days;

    public Long getUserId() {
        return userId;
    }

    public Integer getDays() {
        return days;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setDays(Integer days) {
        this.days = days;
    }


}
