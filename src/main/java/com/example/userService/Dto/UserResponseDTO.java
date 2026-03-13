package com.example.userService.Dto;

import com.example.userService.Entity.User;
import com.example.userService.Enum.UserRole;
import java.time.LocalDateTime;

public class UserResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String personalNumber;
    private String phoneNumber;
    private UserRole role;
    private LocalDateTime createdAt;
    private boolean hasActiveSubscription;

    // ✅ DEFAULT CONSTRUCTOR
    public UserResponseDTO() {
    }

    // ✅ ENDAST EN CONSTRUCTOR - Från User entity
    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.personalNumber = user.getPersonalNumber();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.hasActiveSubscription = false; // Default värde, sätts av service
    }

    // ✅ GETTERS OCH SETTERS (alla befintliga + den nya)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ✅ SUBSCRIPTION STATUS GETTER/SETTER
    // VIKTIGT: Använd 'get' prefix för boolean fields som börjar med "has"
    // Detta säkerställer korrekt JSON serialization med Jackson
    public boolean getHasActiveSubscription() {
        return hasActiveSubscription;
    }

    public void setHasActiveSubscription(boolean hasActiveSubscription) {
        this.hasActiveSubscription = hasActiveSubscription;
    }
}