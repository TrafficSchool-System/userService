package com.example.userService.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {

    @NotBlank(message = "E-posten får inte vara tom")
    @Email(message = "E-posten måste vara giltig")
    private String email; 
    // Tom konstruktor (krävs för JSSON deserializing)
    public LoginRequestDTO(){}

    public LoginRequestDTO(String email){
        this.email = email; 
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString(){
        return "LoginRequest{email='" + email + "'}"; 
    }

}
