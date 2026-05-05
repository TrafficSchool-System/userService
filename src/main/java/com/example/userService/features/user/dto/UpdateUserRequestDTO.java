package com.example.userService.features.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateUserRequestDTO {

    @NotBlank(message = "Förnamn får inte vara tomt")
    @Pattern(regexp = "^[A-Za-zÅÄÖåäö\\s]+$", message = "Förnamn får endast innehålla bokstäver")
    private String firstName;

    @NotBlank(message = "Efternamn får inte vara tomt")
    @Pattern(regexp = "^[A-Za-zÅÄÖåäö\\s]+$", message = "Efternamn får endast innehålla bokstäver")
    private String lastName;

    @NotBlank(message = "E-post får inte vara tom")
    @Email(message = "E-posten måste vara giltig")
    private String email;

    @Pattern(regexp = "^\\d{12}$|^$", message = "Personnummer måste vara 12 siffror (YYYYMMDDXXXX)")
    private String personalNumber;

    @Pattern(regexp = "^\\d{10}$|^$", message = "Mobilnummer måste vara exakt 10 siffror")
    private String phoneNumber;

    public UpdateUserRequestDTO() {
    }

    public UpdateUserRequestDTO(String firstName, String lastName, String email, String personalNumber,
            String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.personalNumber = personalNumber;
        this.phoneNumber = phoneNumber;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
