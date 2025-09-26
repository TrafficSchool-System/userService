package com.example.userService.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity //Säger till spring att detta är en databasmodell
@Table(name = "users") //Tabellnamnet blir "users" i databasen
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Auto-increment
    private Long id;

    @Column(unique = true, nullable = false) //Email måste vara unik och får inte vara tom-
    private String email;

    @Column(nullable = false) // Förnamn måste finnas
    private String firstName; 

    @Column(nullable = false) //Efternamn måste finnas
    private String lastName; 

    @Column(nullable = false)
    private boolean active = true; //Användaren är aktiv som standard

    //-------------------
    
    // Tom konstruktor
    public User() {
    }

    public User(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

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

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "User {id=" + id + ", email=" + email + ", firstName=" + firstName + ", lastName=" + lastName
                + ", active=" + active + "}";
    }
}
