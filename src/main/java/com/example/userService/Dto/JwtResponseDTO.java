package com.example.userService.Dto;


//DTO för att retunera JWT token tillsamans med en användarinfo
//Används när en användare verifierar magic link och får tillbaka autentisering

public class JwtResponseDTO {
    private String token; 
    private String type = "Bearer"; // Standard JWT prefix
    private UserResponseDTO user;


    public JwtResponseDTO(String token, UserResponseDTO user) {
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


    public UserResponseDTO getUser() {
        return user;
    }


    public void setUser(UserResponseDTO user) {
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
