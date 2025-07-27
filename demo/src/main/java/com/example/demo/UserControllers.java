package com.example.demo;
import com.example.demo.User;
import com.example.demo.UserRepositorys;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@CrossOrigin(origins = "${cors.allowed-origins}")
@RequestMapping("/api/user")
public class UserControllers {

    private final UserRepositorys userRepository;

    public UserControllers (UserRepositorys userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/refresh-key")
    public ResponseEntity<Map<String, Object>> refreshUserKey(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String currentKey = request.get("currentKey");

            // Validate inputs
            if (email == null || email.isEmpty() || currentKey == null || currentKey.isEmpty()) {
                response.put("success", false);
                response.put("message", "Both email and current API key are required");
                return ResponseEntity.badRequest().body(response);
            }

            // Find user by email
            User user = userRepository.findByEmail(email);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Verify current key matches
            if (!currentKey.equals(user.getUserKey())) {
                response.put("success", false);
                response.put("message", "Invalid current API key");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Generate and save new key
            String newKey = UUID.randomUUID().toString();
            user.setUserKey(newKey);
            userRepository.save(user);

            response.put("success", true);
            response.put("newKey", newKey);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error refreshing key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}