package com.hms.backend.service;

import com.hms.backend.dto.UserCreationRequest;
import com.hms.backend.dto.UserDto;
import com.hms.backend.model.User;
import com.hms.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto createUser(UserCreationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User(request.getUsername(), passwordEncoder.encode(request.getPassword()), request.getRole());
        user.setForcePasswordChange(true); // Enforced by default for new users
        user = userRepository.save(user);
        return new UserDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserDto::new).collect(Collectors.toList());
    }

    @Transactional
    public UserDto toggleUserStatus(Long userId, boolean enable) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(enable);
        return new UserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto toggleUserLock(Long userId, boolean unlock) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setLocked(!unlock);
        if (unlock) {
            user.setFailedLoginAttempts(0); // Reset failures on unlock
        }
        return new UserDto(userRepository.save(user));
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        // History shift
        user.setPasswordHistory3(user.getPasswordHistory2());
        user.setPasswordHistory2(user.getPasswordHistory1());
        user.setPasswordHistory1(user.getPassword());

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setForcePasswordChange(true); // Force change on next login
        userRepository.save(user);
    }
}
