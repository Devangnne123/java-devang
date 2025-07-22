package com.example.demo;

import com.example.demo.User;
import com.example.demo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://13.203.218.236:3005") // Allow React dev server
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender emailSender;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail());

        if (user == null || !user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid credentials"
            ));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Login successful");
        response.put("email", user.getEmail());
        response.put("userKey", user.getUserKey());
        response.put("name", user.getName()); // Add name if available
        response.put("searchCount", user.getSearchCount()); // Add search count
        response.put("searchLimit",user.getSearchLimit());
        response.put("credits",user.getCredits());
        response.put("key",user.getKey());
        response.put("id",user.getId());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email not found"
            ));
        }

        // Generate a 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setResetPasswordOtp(otp);
        user.setOtpExpiryTime(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)); // OTP valid for 10 minutes
        userRepository.save(user);

        // Send email with OTP
        sendOtpEmail(user.getEmail(), otp);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP sent to your email"
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email not found"
            ));
        }

        // Check if OTP matches and is not expired
        if (!user.getResetPasswordOtp().equals(request.getOtp())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid OTP"
            ));
        }

        if (System.currentTimeMillis() > user.getOtpExpiryTime()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "OTP has expired"
            ));
        }

        // Update password
        user.setPassword(request.getNewPassword());
        user.setResetPasswordOtp(null);
        user.setOtpExpiryTime(0L);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password reset successfully"
        ));
    }

    private void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp + "\n\nThis OTP is valid for 10 minutes.");

        emailSender.send(message);
    }
}

// Add these new request classes
class ForgotPasswordRequest {
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

class ResetPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}

class LoginRequest {
    private String email;
    private String password;

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}


