package com.hms.backend.service;

import com.hms.backend.model.LoginLog;
import com.hms.backend.model.User;
import com.hms.backend.repository.LoginLogRepository;
import com.hms.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int MAX_FAILED_ATTEMPTS = 3;

    public AuthService(UserRepository userRepository, LoginLogRepository loginLogRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void loginSucceeded(String username, String ipAddress) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setFailedLoginAttempts(0);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Fetch previous active log if exists to close it (though handled by session
            // listener ideally)
            LoginLog log = new LoginLog();
            log.setUserId(user.getId());
            log.setUsername(user.getUsername());
            log.setLoginTime(LocalDateTime.now());
            log.setIpAddress(ipAddress);
            log.setSuccess(true);
            loginLogRepository.save(log);
        }
    }

    @Transactional
    public void loginFailed(String username, String ipAddress, String reason) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.isEnabled() && !user.isLocked()) {
            int newFailures = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(newFailures);
            if (newFailures >= MAX_FAILED_ATTEMPTS) {
                user.setLocked(true);
            }
            userRepository.save(user);

            LoginLog log = new LoginLog();
            log.setUserId(user.getId());
            log.setUsername(user.getUsername());
            log.setLoginTime(LocalDateTime.now());
            log.setIpAddress(ipAddress);
            log.setSuccess(false);
            log.setFailureReason("Failed Attempt: " + newFailures + "/" + MAX_FAILED_ATTEMPTS
                    + (user.isLocked() ? " (Locked)" : ""));
            loginLogRepository.save(log);
        } else if (user != null) {
            LoginLog log = new LoginLog();
            log.setUserId(user.getId());
            log.setUsername(user.getUsername());
            log.setLoginTime(LocalDateTime.now());
            log.setIpAddress(ipAddress);
            log.setSuccess(false);
            log.setFailureReason("Account is Locked/Disabled");
            loginLogRepository.save(log);
        } else {
            LoginLog log = new LoginLog();
            log.setUserId(-1L);
            log.setUsername(username);
            log.setLoginTime(LocalDateTime.now());
            log.setIpAddress(ipAddress);
            log.setSuccess(false);
            log.setFailureReason("User doesn't exist");
            loginLogRepository.save(log);
        }
    }

    @Transactional
    public void handleLogout(String username) {
        if (username != null) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                var logs = loginLogRepository.findTop1ByUserIdOrderByLoginTimeDesc(user.getId());
                if (!logs.isEmpty()) {
                    LoginLog lastLog = logs.get(0);
                    if (lastLog.isSuccess() && lastLog.getLogoutTime() == null) {
                        lastLog.setLogoutTime(LocalDateTime.now());
                        loginLogRepository.save(lastLog);
                    }
                }
            }
        }
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid old password");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword()) ||
                (user.getPasswordHistory1() != null && passwordEncoder.matches(newPassword, user.getPasswordHistory1()))
                ||
                (user.getPasswordHistory2() != null && passwordEncoder.matches(newPassword, user.getPasswordHistory2()))
                ||
                (user.getPasswordHistory3() != null
                        && passwordEncoder.matches(newPassword, user.getPasswordHistory3()))) {
            throw new IllegalArgumentException("Cannot reuse any of the last 3 passwords");
        }

        // Shift history
        user.setPasswordHistory3(user.getPasswordHistory2());
        user.setPasswordHistory2(user.getPasswordHistory1());
        user.setPasswordHistory1(user.getPassword());

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setForcePasswordChange(false);
        userRepository.save(user);
    }
}
