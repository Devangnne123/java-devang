package com.example.demo;

import com.example.demo.ExcelData;
import com.example.demo.User;
import java.util.Map;
import java.util.HashMap;
import com.example.demo.ExcelDataRepository;
import com.example.demo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "http://13.232.220.117:3001
")
public class DataController {

    @Autowired
    private ExcelDataRepository excelDataRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/linkedin")
    public ResponseEntity<?> getLinkedInData(
            @RequestBody LinkedinRequest request) {

        try {
            // 1. Verify the user exists with this key
            Optional<User> user = userRepository.findByUserKey(request.getUserKey());
            if (user.isEmpty()) {  // Now correct with Optional
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid user key"
                ));
            }


            // 2. Find data by LinkedIn URL
            ExcelData data = excelDataRepository.findByLinkedinUrl(request.getLinkedinUrl());
            if (data == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "No data found for this LinkedIn URL"
                ));
            }

            // 3. Return the data
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", data
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error processing request"
            ));
        }
    }
}

class LinkedinRequest {
    private String userKey;
    private String linkedinUrl;

    // Getters and setters
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
}