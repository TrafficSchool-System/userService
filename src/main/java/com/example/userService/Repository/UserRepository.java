package com.example.userService.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.userService.Entity.User;

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

     // Räkna användare
     long count();

}
