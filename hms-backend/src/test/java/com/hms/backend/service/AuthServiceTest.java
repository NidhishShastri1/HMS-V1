package com.hms.backend.service;

import com.hms.backend.model.Role;
import com.hms.backend.model.User;
import com.hms.backend.repository.LoginLogRepository;
import com.hms.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginLogRepository loginLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "encodedPassword", Role.RECEPTION);
        testUser.setId(1L);
    }

    @Test
    void loginFailed_shouldIncrementAttemptAndLockAtThree() {
        testUser.setFailedLoginAttempts(2);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        authService.loginFailed("testuser", "127.0.0.1", "Bad credentials");

        assertTrue(testUser.isLocked());
        verify(userRepository, times(1)).save(testUser);
        verify(loginLogRepository, times(1)).save(any());
    }

    @Test
    void loginSucceeded_shouldResetFailedAttempts() {
        testUser.setFailedLoginAttempts(2);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        authService.loginSucceeded("testuser", "127.0.0.1");

        assertEquals(0, testUser.getFailedLoginAttempts());
        assertNotNull(testUser.getLastLogin());
        verify(userRepository, times(1)).save(testUser);
        verify(loginLogRepository, times(1)).save(any());
    }

    @Test
    void changePassword_shouldFailIfReusingPreviousThreePasswords() {
        testUser.setPassword("encodedPassword");
        testUser.setPasswordHistory1("old1");
        testUser.setPasswordHistory2("old2");
        testUser.setPasswordHistory3("old3");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("old1", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.matches("old1", "old1")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword("testuser", "oldPassword", "old1"));
    }

    @Test
    void changePassword_shouldSucceedIfValid() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("newPassword", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncoded");

        authService.changePassword("testuser", "oldPassword", "newPassword");

        assertEquals("newEncoded", testUser.getPassword());
        assertEquals("encodedPassword", testUser.getPasswordHistory1());
        assertFalse(testUser.isForcePasswordChange());
        verify(userRepository, times(1)).save(testUser);
    }
}
