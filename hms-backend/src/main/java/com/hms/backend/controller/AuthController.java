package com.hms.backend.controller;

import com.hms.backend.dto.AuthRequest;
import com.hms.backend.dto.AuthResponse;
import com.hms.backend.dto.ChangePasswordRequest;
import com.hms.backend.model.User;
import com.hms.backend.security.CustomUserDetails;
import com.hms.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Re-create session to prevent session fixation, managed by Spring Security
            // mostly, but explicitly:
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            authService.loginSucceeded(authRequest.getUsername(), ipAddress);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            return ResponseEntity
                    .ok(new AuthResponse(user.getUsername(), user.getRole().name(), user.isForcePasswordChange()));

        } catch (AuthenticationException ex) {
            String reason = (ex instanceof LockedException) ? "Locked out" : "Bad credentials";
            authService.loginFailed(authRequest.getUsername(), ipAddress, reason);
            throw ex; // Let global exception handler catch it
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            authService.handleLogout(auth.getName());
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        authService.changePassword(username, changePasswordRequest.getOldPassword(),
                changePasswordRequest.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
            User user = details.getUser();
            return ResponseEntity
                    .ok(new AuthResponse(user.getUsername(), user.getRole().name(), user.isForcePasswordChange()));
        }
        return ResponseEntity.status(401).build();
    }
}
