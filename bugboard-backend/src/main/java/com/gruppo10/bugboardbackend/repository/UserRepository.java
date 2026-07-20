package com.gruppo10.bugboardbackend.repository;

import com.gruppo10.bugboardbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Boot capisce automaticamente questa firma e crea la query
    // "SELECT * FROM app_users WHERE email = ?"
    Optional<User> findByEmail(String email);
}