package com.hms.backend.dto;

import com.hms.backend.model.Role;
import com.hms.backend.model.User;
import java.time.LocalDateTime;

public class UserDto {
    private Long id;
    private String username;
    private Role role;
    private boolean enabled;
    private boolean locked;
    private LocalDateTime lastLogin;

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.enabled = user.isEnabled();
        this.locked = user.isLocked();
        this.lastLogin = user.getLastLogin();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isLocked() {
        return locked;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
}
