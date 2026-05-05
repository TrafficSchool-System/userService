package com.example.userService.features.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyTokenRequestDTO {

    @NotBlank(message = "Token får inte vara tom")
    private String token;

    public VerifyTokenRequestDTO() {}

    public VerifyTokenRequestDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    } 

    @Override
    public String toString() {
        return "VerifyTokenRequest{token='" + 
               (token != null ? token.substring(0, Math.min(8, token.length())) + "..." : "null") + 
               "'}";
    }
    
    
    

}
