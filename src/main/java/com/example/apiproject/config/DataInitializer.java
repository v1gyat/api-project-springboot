package com.example.apiproject.config;

import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.User;
import com.example.apiproject.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Database initialization - creates default admin user if no admin exists
 */
@Slf4j
@Configuration
public class DataInitializer {

    @Value("${admin.default.name}")
    private String adminName;

    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if any admin user exists
            long adminCount = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == Role.ADMIN)
                    .count();

            // If no admin exists, create default admin
            if (adminCount == 0) {
                User admin = new User();
                admin.setName(adminName);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);

                log.warn("==========================================");
                log.warn("⚠️  DEFAULT ADMIN ACCOUNT CREATED");
                log.warn("==========================================");
                log.warn("Email: {}", adminEmail);
                log.warn("Password: {}", adminPassword);
                log.warn("⚠️  PLEASE CHANGE THIS PASSWORD IMMEDIATELY!");
                log.warn("==========================================");
            }
        };
    }
}
