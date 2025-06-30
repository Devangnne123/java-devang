package com.example.demo;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "userKey")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String userKey;

    @Column(nullable = false)
    private Integer searchCount = 0;

    @Column(nullable = false)
    private Integer searchLimit = 10;

    @Column(nullable = false)
    private Integer credits = 10;

    @Column(nullable = false)
    private Integer searchCount_Cost = 2;

    @Column(nullable = false)
    private Integer key = 1;  // New field with default value 1


    @Column(name = "reset_password_otp")
    private String resetPasswordOtp;

    @Column(name = "otp_expiry_time")
    private Long otpExpiryTime;



    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SearchHistory> searchHistory = new ArrayList<>();

    // Constructors, getters and setters remain the same
    public User() {}

    public User(String name, String email, String password, String userKey) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.userKey = userKey;
    }

    // Getters and Setters (existing + new credits field)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }

    public Integer getSearchCount() { return searchCount; }
    public void setSearchCount(Integer searchCount) { this.searchCount = searchCount; }

    public Integer getSearchLimit() { return searchLimit; }
    public void setSearchLimit(Integer searchLimit) { this.searchLimit = searchLimit; }

    // New getter and setter for credits
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public Integer getSearchCount_Cost() { return searchCount_Cost; }
    public void setSearchCount_Cost(Integer searchCount_Cost) { this.searchCount_Cost = searchCount_Cost; }

    // New getter and setter for key
    public Integer getKey() { return key; }
    public void setKey(Integer key) { this.key = key; }

    // Getters and setters for new fields
    public String getResetPasswordOtp() { return resetPasswordOtp; }
    public void setResetPasswordOtp(String resetPasswordOtp) { this.resetPasswordOtp = resetPasswordOtp; }

    public Long getOtpExpiryTime() { return otpExpiryTime; }
    public void setOtpExpiryTime(Long otpExpiryTime) { this.otpExpiryTime = otpExpiryTime; }

}