package com.example.userService.Repository;

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

     //Bara det vi måste ha för magic links: 
     Optional<LoginToken> findByTokenAndUsedFalse(String Token); 

}
