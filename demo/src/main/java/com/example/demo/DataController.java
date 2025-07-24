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
import java.util.concurrent.locks.ReentrantLock;
import jakarta.servlet.http.HttpServletRequest;

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

    // Lock per user to ensure sequential processing
    private final Map<String, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    private static final int RATE_LIMIT = 500; // requests
    private static final int RATE_LIMIT_WINDOW_MINUTES = 1; // per minute

    @PostMapping("/linkedin")
    public ResponseEntity<?> getLinkedInData(
            @RequestBody LinkedinRequest request,
            HttpServletRequest httpRequest) {

        String userKey = request.getUserKey();
        String clientIp = getClientIp(httpRequest);

        System.out.println("Request received from IP: " + clientIp + " for user: " + userKey);

        ReentrantLock userLock = userLocks.computeIfAbsent(userKey, k -> new ReentrantLock());

        try {
            userLock.lock();

            try {
                // Rate limiting check
                Deque<LocalDateTime> timestamps = requestTimestamps.computeIfAbsent(userKey, k -> new LinkedList<>());
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime cutoff = now.minus(RATE_LIMIT_WINDOW_MINUTES, ChronoUnit.MINUTES);

                while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                    timestamps.removeFirst();
                }

                if (timestamps.size() >= RATE_LIMIT) {
                    return ResponseEntity.status(429).body(Map.of(
                            "success", false,
                            "message", "Rate limit exceeded - maximum " + RATE_LIMIT + " requests per minute",
                            "retryAfterSeconds", ChronoUnit.SECONDS.between(now, timestamps.peekFirst().plusMinutes(1))
                    ));
                }

                // Verify user exists
                Optional<User> userOptional = userRepository.findByUserKey(userKey);
                if (userOptional.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Invalid user key"
                    ));
                }

                User user = userOptional.get();

                // Check search limit
                if (user.getSearchCount() >= user.getSearchLimit()) {
                    return ResponseEntity.ok(Map.of(
                            "success", false,
                            "message", "Search limit reached",
                            "searchCount", user.getSearchCount(),
                            "searchLimit", user.getSearchLimit(),
                            "credits", user.getCredits()
                    ));
                }

                // Check credits
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

                // Normalize LinkedIn URL
                String normalizedUrl = normalizeLinkedInUrl(request.getLinkedinUrl());
                if (normalizedUrl == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Invalid LinkedIn URL format"
                    ));
                }

                // Find data using normalized URL
                ExcelData data = excelDataRepository.findByLinkedinUrl(normalizedUrl);
                if (data == null) {
                    return ResponseEntity.ok(Map.of(
                            "success", false,
                            "message", "No data found for this LinkedIn URL",
                            "normalizedUrl", normalizedUrl
                    ));
                }

                // Update user stats
                user.setCredits(user.getCredits() - user.getSearchCount_Cost());
                user.setSearchCount(user.getSearchCount() + 1);
                userRepository.save(user);

                // Record history with IP address
                SearchHistory history = new SearchHistory();
                history.setUser(user);
                history.setLinkedinUrl(normalizedUrl);
                history.setClientIp(clientIp);
                history.setCreditsDeducted(user.getSearchCount_Cost());
                history.setSearchCount(user.getSearchCount());
                history.setRemainingCredits(user.getCredits());
                history.setSearchLimit(user.getSearchLimit());
                history.setSearchDate(now);
                searchHistoryRepository.save(history);

                // Update rate limit
                timestamps.addLast(now);
                requestTimestamps.put(userKey, timestamps);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", data,
                        "searchCount", user.getSearchCount(),
                        "searchLimit", user.getSearchLimit(),
                        "credits", user.getCredits(),
                        "clientIp", clientIp,
                        "normalizedUrl", normalizedUrl
                ));

            } finally {
                userLock.unlock();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error processing request",
                    "clientIp", clientIp
            ));
        }
    }

    /**
     * Normalizes LinkedIn URLs to standard format:
     * - Removes protocol (http/https)
     * - Removes www.
     * - Converts to lowercase
     * - Ensures it starts with linkedin.com/
     */
    private String normalizeLinkedInUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        // Convert to lowercase
        url = url.toLowerCase().trim();

        // Remove protocol if present
        if (url.startsWith("http://")) {
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            url = url.substring(8);
        }

        // Remove www. if present
        if (url.startsWith("www.")) {
            url = url.substring(4);
        }

        // Ensure it starts with linkedin.com
        if (!url.startsWith("linkedin.com/")) {
            return null;  // Invalid LinkedIn URL format
        }

        // Remove any trailing slashes or query parameters
        int queryIndex = url.indexOf('?');
        if (queryIndex > 0) {
            url = url.substring(0, queryIndex);
        }

        int hashIndex = url.indexOf('#');
        if (hashIndex > 0) {
            url = url.substring(0, hashIndex);
        }

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // In case of multiple IPs
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }
}

class LinkedinRequest {
    private String userKey;
    private String linkedinUrl;

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }
}