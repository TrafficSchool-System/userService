package com.example.userService.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterRequestDTO {

    

    @NotBlank(message = "Namn får inte vara tom")
    private String firstName; 

    @NotBlank(message = "Efternamn får inte vara tom")
    private String lastName;

    @NotBlank(message = "E-psoten får inte vara tom")
    @Email(message = "E-posten måste vara giltig")
    private String email; 

    public RegisterRequestDTO(){}

    public RegisterRequestDTO(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
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

    
    

    

}
