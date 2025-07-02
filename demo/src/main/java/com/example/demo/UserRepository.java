package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;


public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    Optional<User> findByUserKey(String userKey);  // Changed to return Optional
    boolean existsByEmail(String email);
    boolean existsByUserKey(String userKey);

    User findByEmailAndUserKey(String email, String userKey); // Add this method


}

