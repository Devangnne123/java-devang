package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/credits")
public class CreditHistoryController {

    @Autowired
    private CreditHistoryRepository creditHistoryRepository;

    @Autowired
    private UserRepository userRepository;

//    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<CreditHistory>> getUserCreditHistory(
//            @PathVariable Long userId,
//            @RequestHeader("X-User-Email") String email)
//    {
//
//        List<CreditHistory> history = creditHistoryRepository.findByUserIdOrderByTimestampDesc(userId);
//        return ResponseEntity.ok(history);
//    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserCreditHistory(
            @PathVariable Long userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Key") String userKey) {

        // Authenticate the requesting user
        User requestingUser = userRepository.findByEmailAndUserKey(email, userKey);
        if (requestingUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authentication failed");
        }

        // For non-admin users, they can only view their own history
        if (!requestingUser.getKey().equals(23) && !requestingUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized access");
        }

        // Get the history for the requested user
        List<CreditHistory> history = creditHistoryRepository.findByUserIdOrderByTimestampDesc(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<CreditHistory>> getAdminCreditHistory(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Key") Integer key) {

        // Verify admin access
        User user = userRepository.findByEmail(email);
        if (user == null || user.getKey() != 23 || key != 23) {
            return ResponseEntity.status(403).build();
        }

        List<CreditHistory> history = creditHistoryRepository.findByAdminEmailOrderByTimestampDesc(email);
        return ResponseEntity.ok(history);
    }







}
