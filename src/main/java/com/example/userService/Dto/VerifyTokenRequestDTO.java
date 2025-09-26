package com.example.userService.Dto;

public class VerifyTokenRequestDTO {

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
