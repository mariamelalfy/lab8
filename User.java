package model;

public abstract class User {
    protected String userId;
    protected String username;
    protected String email;
    protected String passwordHash;
    protected String role; // whether user is a student, instructor or admin
    
    public User(String userId, String username, String email, 
                String passwordHash, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }
    
    public abstract void displayDashboard();
    
    public String getUserId() {
        return userId;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public String getRole() {
        return role;
    }
    
    public void setUserId(String userId) { 
        this.userId = userId; 
    }
    public void setUsername(String username) { 
        this.username = username;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash; 
    }
    public void setRole(String role) {
        this.role = role;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
    
}