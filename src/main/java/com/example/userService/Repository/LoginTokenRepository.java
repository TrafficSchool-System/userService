package com.example.userService.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.userService.Entity.LoginToken;

public interface LoginTokenRepository extends JpaRepository<LoginToken, Long> {
    /** Spring skapar automatiskt:
     * save()
     * findById()
     * findAll
     * delete()
     */ 

     // Hitta aktiv token
    Optional<LoginToken> findByTokenAndUsedFalse(String token);
    
    // Spring skapar automatiskt dessa metoder:
    List<LoginToken> findByEmailAndUsedFalse(String email);
    void deleteByEmailAndUsedFalse(String email); 

}
