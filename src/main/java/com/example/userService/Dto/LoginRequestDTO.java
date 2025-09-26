package com.example.userService.Dto;

public class LoginRequestDTO {

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
