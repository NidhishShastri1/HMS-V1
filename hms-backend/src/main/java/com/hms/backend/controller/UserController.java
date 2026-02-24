package com.hms.backend.controller;

import com.hms.backend.dto.AdminResetPasswordRequest;
import com.hms.backend.dto.UserCreationRequest;
import com.hms.backend.dto.UserDto;
import com.hms.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreationRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<UserDto> toggleStatus(@PathVariable Long id, @RequestParam boolean enable) {
        return ResponseEntity.ok(userService.toggleUserStatus(id, enable));
    }

    @PutMapping("/{id}/toggle-lock")
    public ResponseEntity<UserDto> toggleLock(@PathVariable Long id, @RequestParam boolean unlock) {
        return ResponseEntity.ok(userService.toggleUserLock(id, unlock));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id,
            @Valid @RequestBody AdminResetPasswordRequest request) {
        userService.resetPassword(id, request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
