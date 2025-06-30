package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    Optional<User> findByUserKey(String userKey);  // Changed to return Optional
    boolean existsByEmail(String email);
    boolean existsByUserKey(String userKey);

    User findByEmailAndKey(String email, Integer key); // Add this method

}

