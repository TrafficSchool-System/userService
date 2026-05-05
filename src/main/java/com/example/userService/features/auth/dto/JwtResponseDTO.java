package com.example.userService.features.auth.dto;

/**
 * JWT Response DTO
 * 
 * Response object for JWT token generation.
 * Contains the signed JWT token and minimal user authentication context.
 * 
 * Used when a user verifies magic link and receives authentication.
 * Decoupled from User domain - uses AuthUserContext instead of UserResponseDTO.
 */
public class JwtResponseDTO {

    private String token;
    private String type = "Bearer";
    private AuthUserContext user;

    public JwtResponseDTO(String token, AuthUserContext user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AuthUserContext getUser() {
        return user;
    }

    public void setUser(AuthUserContext user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "JwtResponseDTO{" +
                "token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", user=" + user +
                '}';
    }
}
