package com.example.userService.features.logintoken.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.userService.features.logintoken.entity.LoginToken;

import jakarta.transaction.Transactional;

public interface LoginTokenRepository extends JpaRepository<LoginToken, Long> {

    // Hitta aktiv token
    Optional<LoginToken> findByTokenAndUsedFalse(String token);

    // Spring skapar automatiskt dessa metoder:
    List<LoginToken> findByEmailAndUsedFalse(String email);

    // Hitta ALLA tokens för en email (även använda) - används vid cascade delete
    List<LoginToken> findByEmail(String email);

    // Ta bort alla tokens för en email (används vid user cascade delete)
    @Transactional
    void deleteByEmail(String email);

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
