package com.example.demo;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;
    private String userKey;  // renamed from 'key' as 'key' is a reserved word

    private Integer searchCount = 0;
    private Integer searchLimit = 10;

    // New field: Credits (initialized to 0 by default)
    private Integer credits = 10;

    private Integer searchCount_Cost =2;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SearchHistory> searchHistory = new ArrayList<>();

    // Getters & Setters
    public List<SearchHistory> getSearchHistory() { return searchHistory; }
    public void setSearchHistory(List<SearchHistory> searchHistory) {
        this.searchHistory = searchHistory;
    }




    // Constructors
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


}