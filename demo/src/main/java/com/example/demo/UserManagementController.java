package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "${cors.allowed-origins}")
@RequestMapping("/api/users")
public class UserManagementController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditHistoryRepository creditHistoryRepository;

    private static final int ADMIN_KEY = 23;

    private ResponseEntity<?> verifyAdminAccess(String email, Integer key) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication required: Email header is missing");
        }

        if (key == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication required: Key header is missing");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with email: " + email);
        }

        if (user.getKey() != ADMIN_KEY || key != ADMIN_KEY) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Admin privileges required");
        }

        return null;
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Key") Integer key) {

        ResponseEntity<?> accessCheck = verifyAdminAccess(email, key);
        if (accessCheck != null) return accessCheck;

        Page<User> usersPage = userRepository.findAll(PageRequest.of(page, size));
        List<UserDTO> userDTOs = usersPage.getContent().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(usersPage.getTotalElements()))
                .body(userDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Key") Integer key) {

        ResponseEntity<?> accessCheck = verifyAdminAccess(email, key);
        if (accessCheck != null) return accessCheck;

        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestBody UserCreateRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Key") Integer key) {

        ResponseEntity<?> accessCheck = verifyAdminAccess(email, key);
        if (accessCheck != null) return accessCheck;

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Error: Email is already taken!");
        }

        if (request.getUserKey() != null && !request.getUserKey().isEmpty() &&
                userRepository.existsByUserKey(request.getUserKey())) {
            return ResponseEntity.badRequest()
                    .body("Error: UserKey is already taken!");
        }

        User newUser = new User(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getUserKey()
        );
        newUser.setCredits(request.getCredits() != null ? request.getCredits() : 10);
        newUser.setSearchCount_Cost(request.getSearchCount_Cost() != null ? request.getSearchCount_Cost() : 2);
        newUser.setSearchLimit(request.getSearchLimit() != null ? request.getSearchLimit() : 10);
        newUser.setKey(request.getKey() != null ? request.getKey() : 1);

        User savedUser = userRepository.save(newUser);
        return ResponseEntity.ok(new UserDTO(savedUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Key") Integer key) {

        ResponseEntity<?> accessCheck = verifyAdminAccess(email, key);
        if (accessCheck != null) return accessCheck;

        return userRepository.findById(id)
                .map(user -> {
                    int oldCredits = user.getCredits();

                    if (request.getCredits() != null) {
                        int newCredits = user.getCredits() + request.getCredits();
                        user.setCredits(Math.max(0, newCredits));

                        // Record credit history
                        if (request.getCredits() != 0) {
                            CreditHistory history = new CreditHistory();
                            history.setUser(user);
                            history.setAmount(Math.abs(request.getCredits()));
                            history.setActionType(request.getCredits() > 0 ? "ADD" : "DEDUCT");
                            history.setDescription(request.getDescription() != null ?
                                    request.getDescription() : "Admin adjustment");
                            history.setAdminEmail(email);
                            creditHistoryRepository.save(history);
                        }
                    }

                    if (request.getSearchCount_Cost() != null) {
                        user.setSearchCount_Cost(Math.max(1, request.getSearchCount_Cost()));
                    }

                    if (request.getSearchCount() != null) {
                        user.setSearchCount(Math.max(0, request.getSearchCount()));
                    }

                    if (request.getSearchLimit() != null) {
                        user.setSearchLimit(Math.max(0, request.getSearchLimit()));
                    }

                    if (request.getUserKey() != null) {
                        user.setUserKey(request.getUserKey());
                    }

                    if (request.getKey() != null) {
                        user.setKey(request.getKey());
                    }

                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(new UserDTO(updatedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Key") Integer key) {

        ResponseEntity<?> accessCheck = verifyAdminAccess(email, key);
        if (accessCheck != null) return accessCheck;

        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
class UserCreateRequest {
    private String name;
    private String email;
    private String password;
    private String userKey;
    private Integer credits;
    private Integer searchCount_Cost;
    private Integer searchLimit;
    private Integer key;


    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public Integer getSearchCount_Cost() { return searchCount_Cost; }
    public void setSearchCount_Cost(Integer searchCount_Cost) {
        this.searchCount_Cost = searchCount_Cost;
    }
    public Integer getSearchLimit() { return searchLimit; }
    public void setSearchLimit(Integer searchLimit) { this.searchLimit = searchLimit; }
    public Integer getKey() { return key; }
    public void setKey(Integer key) { this.key = key; }
}

class UserUpdateRequest {
    private String name;
    private String email;
    private String password;
    private String userKey;
    private Integer credits;
    private Integer searchCount_Cost;
    private Integer searchCount;
    private Integer searchLimit;
    private Integer key;

    private String description; // New field for history description


    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public Integer getSearchCount_Cost() { return searchCount_Cost; }
    public void setSearchCount_Cost(Integer searchCount_Cost) {
        this.searchCount_Cost = searchCount_Cost;
    }
    public Integer getSearchCount() { return searchCount; }
    public void setSearchCount(Integer searchCount) { this.searchCount = searchCount; }
    public Integer getSearchLimit() { return searchLimit; }
    public void setSearchLimit(Integer searchLimit) { this.searchLimit = searchLimit; }
    public Integer getKey() { return key; }
    public void setKey(Integer key) { this.key = key; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String userKey;
    private Integer searchCount;
    private Integer searchLimit;
    private Integer credits;
    private Integer searchCount_Cost;
    private Integer key;

    public UserDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.userKey = user.getUserKey();
        this.searchCount = user.getSearchCount();
        this.searchLimit = user.getSearchLimit();
        this.credits = user.getCredits();
        this.searchCount_Cost = user.getSearchCount_Cost();
        this.key = user.getKey();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public Integer getSearchCount() { return searchCount; }
    public void setSearchCount(Integer searchCount) { this.searchCount = searchCount; }
    public Integer getSearchLimit() { return searchLimit; }
    public void setSearchLimit(Integer searchLimit) { this.searchLimit = searchLimit; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public Integer getSearchCount_Cost() { return searchCount_Cost; }
    public void setSearchCount_Cost(Integer searchCount_Cost) { this.searchCount_Cost = searchCount_Cost; }
    public Integer getKey() { return key; }
    public void setKey(Integer key) { this.key = key; }
}