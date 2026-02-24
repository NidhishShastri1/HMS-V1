package com.hms.backend.dto;

import com.hms.backend.model.LoginLog;
import java.time.LocalDateTime;

public class LoginLogDto {
    private Long id;
    private Long userId;
    private String username;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String ipAddress;
    private boolean success;
    private String failureReason;

    public LoginLogDto(LoginLog log) {
        this.id = log.getId();
        this.userId = log.getUserId();
        this.username = log.getUsername();
        this.loginTime = log.getLoginTime();
        this.logoutTime = log.getLogoutTime();
        this.ipAddress = log.getIpAddress();
        this.success = log.isSuccess();
        this.failureReason = log.getFailureReason();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public LocalDateTime getLogoutTime() {
        return logoutTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
