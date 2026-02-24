package com.hms.backend.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean locked;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts;

    @Column(name = "force_password_change")
    private boolean forcePasswordChange;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "password_history_1")
    private String passwordHistory1;

    @Column(name = "password_history_2")
    private String passwordHistory2;

    @Column(name = "password_history_3")
    private String passwordHistory3;

    public User() {
    }

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = true;
        this.locked = false;
        this.failedLoginAttempts = 0;
        this.forcePasswordChange = true;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getPasswordHistory1() {
        return passwordHistory1;
    }

    public void setPasswordHistory1(String passwordHistory1) {
        this.passwordHistory1 = passwordHistory1;
    }

    public String getPasswordHistory2() {
        return passwordHistory2;
    }

    public void setPasswordHistory2(String passwordHistory2) {
        this.passwordHistory2 = passwordHistory2;
    }

    public String getPasswordHistory3() {
        return passwordHistory3;
    }

    public void setPasswordHistory3(String passwordHistory3) {
        this.passwordHistory3 = passwordHistory3;
    }
}
