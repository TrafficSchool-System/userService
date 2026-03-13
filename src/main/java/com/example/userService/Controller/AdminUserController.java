package com.example.userService.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userService.Dto.UpdateUserRequestDTO;
import com.example.userService.Dto.UserResponseDTO;
import com.example.userService.Service.user.UserServiceInterface;

import jakarta.validation.Valid;

import java.util.List;

/**
 * ADMIN USER CONTROLLER
 * 
 * RESTful endpoints for admin user management operations.
 * Base path: /api/admin/users
 * 
 * ADMIN OPERATIONS:
 * - GET /admin/users : List all users in the system
 * - GET /admin/users/{id} : View specific user details
 * - PUT /admin/users/{id} : Update user information
 * - GET /admin/users/statistics : Get user statistics (total count, etc.)
 * 
 * AUTHENTICATION:
 * - All endpoints require ADMIN or INTERNAL_SERVICE role
 * - ADMIN: Via X-User-Role header from API Gateway
 * - INTERNAL_SERVICE: Via X-Internal-API-Key from other microservices
 * 
 * DESIGN PATTERN:
 * - Admin endpoints are separated from user endpoints
 * - Focus on aggregation and monitoring, not core CRUD
 * - Used by AdminService for dashboard and user management
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAnyRole('ADMIN', 'INTERNAL_SERVICE')")
public class AdminUserController {

    @Autowired
    private UserServiceInterface userService;

    /**
     * LIST ALL USERS
     * GET /api/admin/users
     * 
     * Returns all users in the system for admin monitoring.
     * Used by admin dashboard to display user list and analytics.
     * 
     * @return List of all users with their profile information
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET USER BY ID
     * GET /api/admin/users/{id}
     * 
     * Returns specific user details for admin monitoring.
     * Used to view user profile, subscription status, and activity.
     * 
     * @param id User ID to retrieve
     * @return User profile information
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * UPDATE USER
     * PUT /api/admin/users/{id}
     * 
     * Allows admin to update user information.
     * Used for admin user management operations.
     * 
     * @param id            User ID to update
     * @param updateRequest Updated user information
     * @return Updated user profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDTO updateRequest) {
        UserResponseDTO updatedUser = userService.updateUser(id, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * GET USER STATISTICS
     * GET /api/admin/users/statistics
     * 
     * Returns aggregated user statistics for admin dashboard.
     * Includes total user count and other metrics.
     * 
     * @return User statistics (total count)
     */
    @GetMapping("/statistics")
    public ResponseEntity<Long> getUserStatistics() {
        long count = userService.countTotalUsers();
        return ResponseEntity.ok(count);
    }

    /**
     * DELETE USER
     * DELETE /api/admin/users/{id}
     * 
     * Permanently removes a user from the system.
     * Used by admin for user management operations.
     * 
     * WARNING: This is a permanent operation and cannot be undone.
     * 
     * @param id User ID to delete
     * @return Empty response with 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
