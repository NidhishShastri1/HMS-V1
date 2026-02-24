package com.hms.backend.dto;

public class AuthResponse {
    private String username;
    private String role;
    private boolean forcePasswordChange;

    public AuthResponse(String username, String role, boolean forcePasswordChange) {
        this.username = username;
        this.role = role;
        this.forcePasswordChange = forcePasswordChange;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }
}
