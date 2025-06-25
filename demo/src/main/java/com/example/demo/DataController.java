package com.example.demo;

import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "http://3.109.203.132:3001")
public class DataController {

    @Autowired
    private ExcelDataRepository excelDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @PostMapping("/linkedin")
    public ResponseEntity<?> getLinkedInData(
            @RequestBody LinkedinRequest request) {

        try {
            // 1. Verify the user exists with this key
            Optional<User> userOptional = userRepository.findByUserKey(request.getUserKey());
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid user key"
                ));
            }

            User user = userOptional.get();

            // 2. Check if user has reached search limit
            if (user.getSearchCount() >= user.getSearchLimit()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Search limit reached",
                        "searchCount", user.getSearchCount(),
                        "searchLimit", user.getSearchLimit(),
                        "credits", user.getCredits()
                ));
            }

            // 3. Check if user has enough credits
            if (user.getCredits() < user.getSearchCount_Cost()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Insufficient credits",
                        "searchCount", user.getSearchCount(),
                        "searchLimit", user.getSearchLimit(),
                        "credits", user.getCredits(),
                        "searchCost", user.getSearchCount_Cost()
                ));
            }

            // 4. Find data by LinkedIn URL
            ExcelData data = excelDataRepository.findByLinkedinUrl(request.getLinkedinUrl());
            if (data == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "No data found for this LinkedIn URL"
                ));
            }

            // 5. Deduct credits, increment search count, and save the user
            user.setCredits(user.getCredits() - user.getSearchCount_Cost());
            user.setSearchCount(user.getSearchCount() + 1);
            userRepository.save(user);

            // 6. Record the search in history
            SearchHistory history = new SearchHistory();
            history.setUser(user);
            history.setLinkedinUrl(request.getLinkedinUrl());
            history.setCreditsDeducted(user.getSearchCount_Cost());
            history.setSearchCount(user.getSearchCount());
            history.setRemainingCredits(user.getCredits());

            history.setSearchLimit(user.getSearchLimit());
            history.setSearchDate(LocalDateTime.now());
            searchHistoryRepository.save(history);

            // 7. Return the data with updated counts
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", data,
                    "searchCount", user.getSearchCount(),
                    "searchLimit", user.getSearchLimit(),
                    "credits", user.getCredits()
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