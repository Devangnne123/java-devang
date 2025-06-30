import com.example.demo.User;

public class UserDTO {
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