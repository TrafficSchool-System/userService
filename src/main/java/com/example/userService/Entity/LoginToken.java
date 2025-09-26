package com.example.userService.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class LoginToken {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id; 

    @Column(nullable = false, unique = true)
    private String token; //Magiska token som ska skicas via mejl

    @Column(nullable = false)
    private String email; //Vilken email tokenet tillhör

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; //När token går ut (ex 5 min)

    @Column(nullable = false)
    private boolean used = false; //Om token redan använts (en gång bara)

    public LoginToken() {
    }

    public LoginToken(String token, String email, LocalDateTime expiresAt) {
        this.token = token;
        this.email = email;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @Override
    public String toString() {
        return "LoginToken {id=" + id + ", token=" + token.substring(0, 8) + "...." + ", email=" + email + ", expiresAt=" + expiresAt + ", used="
                + used + "}";
    }
    
    
    

}
