package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "${cors.allowed-origins}")
@RequestMapping("/api/user")
public class UserController_H {

    private final UserRepositorys userRepositorys;
    private final SearchHistoryRepository searchHistoryRepository;

    public UserController_H(UserRepositorys userRepositorys, SearchHistoryRepository searchHistoryRepository) {
        this.userRepositorys = userRepositorys;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    @GetMapping("/history")
    public ResponseEntity<?> getSearchHistory(@RequestParam String userKey) {
        try {
            User user = userRepositorys.findByUserKey(userKey);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid user key"
                ));
            }

            List<SearchHistory> history = searchHistoryRepository.findByUserOrderBySearchDateDesc(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "history", history.stream().map(this::convertToDto).collect(Collectors.toList())
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to fetch history"
            ));
        }
    }

    private Map<String, Object> convertToDto(SearchHistory history) {
        return Map.of(
                "searchDate", history.getSearchDate(),
                "linkedinUrl", history.getLinkedinUrl(),
                "creditsDeducted", history.getCreditsDeducted(),
                "remainingCredits", history.getRemainingCredits(),
                "searchCount", history.getSearchCount(),
                "searchLimit", history.getSearchLimit(),
                "originalUrl", history.getOriginalUrl(),
                "wasSuccessful", history.getWasSuccessful(),
                "clientIp", history.getClientIp()
        );
    }
}