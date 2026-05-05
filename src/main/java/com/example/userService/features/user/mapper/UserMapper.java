package com.example.userService.features.user.mapper;

import org.springframework.stereotype.Component;

import com.example.userService.features.user.dto.UserResponseDTO;
import com.example.userService.features.user.entity.User;

@Component
public class UserMapper {

    public UserResponseDTO toResponse(User user, boolean hasActiveSubscription) {
        UserResponseDTO dto = new UserResponseDTO(user);
        dto.setHasActiveSubscription(hasActiveSubscription);
        return dto;
    }
}