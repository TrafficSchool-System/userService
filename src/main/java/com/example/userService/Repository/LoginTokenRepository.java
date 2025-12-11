package com.example.userService.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.userService.Entity.LoginToken;

import jakarta.transaction.Transactional;

public interface LoginTokenRepository extends JpaRepository<LoginToken, Long> {
    
     // Hitta aktiv token
    Optional<LoginToken> findByTokenAndUsedFalse(String token);
    
    // Spring skapar automatiskt dessa metoder:
    List<LoginToken> findByEmailAndUsedFalse(String email);

    // Ta bort oanvända tokens för en email (spring skapar automatiskt)
    @Transactional
    void deleteByEmailAndUsedFalse(String email);

    // Ta bort alla utgågna tokens
    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime expiresAt); 
    
    // Ta bort alla använda tokens
    @Transactional
    void deleteByUsedTrue();

}
