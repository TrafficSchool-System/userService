package com.example.userService.features.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.userService.features.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    /** Spring skapar automatiskt:
     * save()
     * findById()
     * findAll
     * delete()
     */ 

     //Bara de vi måste ha för passwordless login:
     Optional<User> findByEmail(String email); 
     boolean existsByEmail(String email);  
     boolean existsByPersonalNumber(String personalNumber);

     // Räkna användare
     long count();

}
