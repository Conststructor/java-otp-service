package otpservice.model;

public class User {
    private Long id;
    private String username;
    private Role role;
    private String passwordHash;

    public User() {}
    public User(Long id, String username, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}