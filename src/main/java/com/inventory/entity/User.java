package com.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private Boolean vip = false;

    @Column(nullable = false, length = 20)
    private String role = "USER";

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "industry_mode", length = 20)
    private String industryMode = "mixed";

    @Column(name = "session_token", length = 64)
    private String sessionToken;

    @Column(length = 100)
    private String email;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Boolean getVip() { return vip; }
    public void setVip(Boolean vip) { this.vip = vip; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getIndustryMode() { return industryMode; }
    public void setIndustryMode(String industryMode) { this.industryMode = industryMode; }
    
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}