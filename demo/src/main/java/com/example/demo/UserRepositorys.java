package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepositorys extends JpaRepository<User, Long> {

    // Find user by email
    User findByEmail(String email);

    // Find user by API key
    User findByUserKey(String userKey);

    // Optional: Find user by email AND verify key matches
    default boolean verifyUserKey(String email, String userKey) {
        User user = findByEmail(email);
        return user != null && userKey.equals(user.getUserKey());
    }
}