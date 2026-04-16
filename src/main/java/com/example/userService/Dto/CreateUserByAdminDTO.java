package com.example.userService.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin creating a new user
 * 
 * Same fields as RegisterRequestDTO (email, firstName, lastName,
 * personalNumber, phoneNumber).
 * User will receive magic link to set their password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserByAdminDTO {

    @NotBlank(message = "Förnamn får inte vara tom")
    @Pattern(regexp = "^[A-Za-zÅÄÖåäö\\s]+$", message = "Förnamn får endast innehålla bokstäver")
    private String firstName;

    @NotBlank(message = "Efternamn får inte vara tom")
    @Pattern(regexp = "^[A-Za-zÅÄÖåäö\\s]+$", message = "Efternamn får endast innehålla bokstäver")
    private String lastName;

    @NotBlank(message = "E-posten får inte vara tom")
    @Email(message = "E-posten måste vara giltig")
    private String email;

    @NotBlank(message = "Personnummer får inte vara tomt")
    @Pattern(regexp = "^\\d{12}$", message = "Personnummer måste vara 12 siffror (YYYYMMDDXXXX)")
    private String personalNumber;

    @NotBlank(message = "Mobilnummer får inte vara tomt")
    @Pattern(regexp = "^\\d{10}$", message = "Mobilnummer måste vara exakt 10 siffror")
    private String phoneNumber;
}
