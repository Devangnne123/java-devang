package com.example.demo;

import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Deque;
import java.util.LinkedList;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "http://13.203.218.236:3001")
public class DataController {

    @Autowired
    private ExcelDataRepository excelDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    // Rate limiting storage - tracks request timestamps per user
    private final Map<String, Deque<LocalDateTime>> requestTimestamps = new ConcurrentHashMap<>();
    private static final int RATE_LIMIT = 100; // 10 requests
    private static final int RATE_LIMIT_WINDOW_MINUTES = 1; // per minute

    @PostMapping("/linkedin")
    public ResponseEntity<?> getLinkedInData(
            @RequestBody LinkedinRequest request) {

        try {
            // 0. Check rate limit
            String userKey = request.getUserKey();
            Deque<LocalDateTime> timestamps = requestTimestamps.computeIfAbsent(userKey, k -> new LinkedList<>());

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime cutoff = now.minus(RATE_LIMIT_WINDOW_MINUTES, ChronoUnit.MINUTES);

            // Remove old timestamps outside the 1-minute window
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.removeFirst();
            }

            // Check if limit exceeded
            if (timestamps.size() >= RATE_LIMIT) {
                return ResponseEntity.status(429).body(Map.of(
                        "success", false,
                        "message", "Rate limit exceeded - maximum " + RATE_LIMIT + " requests per minute",
                        "retryAfterSeconds", ChronoUnit.SECONDS.between(now, timestamps.peekFirst().plusMinutes(1))
                ));
            }

            // 1. Verify the user exists with this key
            Optional<User> userOptional = userRepository.findByUserKey(userKey);
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
            history.setSearchDate(now);
            searchHistoryRepository.save(history);

            // Update rate limit tracking
            timestamps.addLast(now);
            requestTimestamps.put(userKey, timestamps);

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