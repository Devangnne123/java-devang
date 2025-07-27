package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.*;
@Entity
@Table(name = "search_history")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_url", length = 512)
    private String originalUrl;       // The original URL as entered by user
    private String linkedinUrl;       // What was searched
    private Integer creditsDeducted;  // How many credits were used
    private LocalDateTime searchDate; // When it happened

    private Integer searchCount;      // How many times this URL was searched
    private Integer searchLimit;


    private Integer remainingCredits;

    @Column(name = "client_ip")
    private String clientIp;

    @Column(name = "was_successful")
    private Boolean wasSuccessful;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }


    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public Integer getCreditsDeducted() { return creditsDeducted; }


    public void setCreditsDeducted(Integer creditsDeducted) { this.creditsDeducted = creditsDeducted; }
    public LocalDateTime getSearchDate() { return searchDate; }
    public void setSearchDate(LocalDateTime searchDate) { this.searchDate = searchDate; }


    // Getters & Setters
    public Integer getSearchCount() { return searchCount; }
    public void setSearchCount(Integer searchCount) { this.searchCount = searchCount; }
    public Integer getSearchLimit() { return searchLimit; }
    public void setSearchLimit(Integer searchLimit) { this.searchLimit = searchLimit; }



    public Integer getRemainingCredits() { return remainingCredits; }
    public void setRemainingCredits(Integer remainingCredits) { this.remainingCredits = remainingCredits; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public Boolean getWasSuccessful() {
        return wasSuccessful;
    }

    public void setWasSuccessful(Boolean wasSuccessful) {
        this.wasSuccessful = wasSuccessful;
    }
}