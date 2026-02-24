package com.hms.backend.config;

import com.hms.backend.model.Role;
import com.hms.backend.model.User;
import com.hms.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);
                admin.setLocked(false);
                admin.setFailedLoginAttempts(0);
                admin.setForcePasswordChange(true);
                userRepository.save(admin);

                System.out.println("Default admin user created: admin / Admin@123");
            }
        };
    }
}
