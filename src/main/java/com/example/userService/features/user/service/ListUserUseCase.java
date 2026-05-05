package com.example.userService.features.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.userService.features.subscription.service.BatchHasActiveSubscriptionsUseCase;
import com.example.userService.features.user.dto.UserResponseDTO;
import com.example.userService.features.user.entity.User;
import com.example.userService.features.user.mapper.UserMapper;
import com.example.userService.features.user.repository.UserRepository;

import java.util.List;
import java.util.Map;

/**
 * USE CASE: List All Users
 * 
 * Handles retrieval of multiple users:
 * 1. Fetches all users from database
 * 2. Batch-loads subscription status (avoids N+1 queries)
 * 3. Converts to DTOs with subscription information
 * 
 * Optimized for performance with batch loading.
 * This is a dedicated use-case service following vertical slice architecture.
 */
@Service
public class ListUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListUserUseCase.class);

    private final UserRepository userRepository;
    private final BatchHasActiveSubscriptionsUseCase batchHasActiveSubscriptionsUseCase;
    private final UserMapper userMapper;

    public ListUserUseCase(
            UserRepository userRepository,
            BatchHasActiveSubscriptionsUseCase batchHasActiveSubscriptionsUseCase,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.batchHasActiveSubscriptionsUseCase = batchHasActiveSubscriptionsUseCase;
        this.userMapper = userMapper;
    }

    /**
     * Find all users in the system
     * Used by AdminService to display user list.
     * 
     * PERFORMANCE OPTIMIZATION:
     * - Uses batch loading for subscription status
     * - Avoids N+1 query problem
     * - Single database call for all subscription checks
     * 
     * @return List of UserResponseDTO with subscription status for each user
     */
    public List<UserResponseDTO> findAllUsers() {
        log.info("Fetching all users with batch-loaded subscription status");

        // STEP 1: Fetch all users from database
        List<User> users = userRepository.findAll();
        log.debug("Found {} users", users.size());

        if (users.isEmpty()) {
            log.info("No users found in database");
            return List.of();
        }

        // STEP 2: Extract user IDs for batch query
        List<Long> userIds = users.stream()
                .map(User::getId)
                .toList();

        // STEP 3: Batch-load subscription status for all users (ONE query)
        Map<Long, Boolean> subscriptionStatusMap = batchHasActiveSubscriptionsUseCase.execute(userIds);
        log.debug("Batch-loaded subscription status for {} users", userIds.size());

        // STEP 4: Convert to DTOs with subscription status
        List<UserResponseDTO> response = users.stream()
                .map(user -> {
                    boolean hasActiveSubscription = subscriptionStatusMap.getOrDefault(user.getId(), false);
                    return userMapper.toResponse(user, hasActiveSubscription);
                })
                .toList();

        log.info("Successfully converted {} users to DTOs", response.size());
        return response;
    }
}