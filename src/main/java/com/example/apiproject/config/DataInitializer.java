package com.example.apiproject.config;

import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.User;
import com.example.apiproject.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Database initialization - creates default admin user if no admin exists
 */
@Configuration
public class DataInitializer {

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
                admin.setName("Admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123")); // Change this password!
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);

                System.out.println("==========================================");
                System.out.println("⚠️  DEFAULT ADMIN ACCOUNT CREATED");
                System.out.println("==========================================");
                System.out.println("Email: admin@example.com");
                System.out.println("Password: admin123");
                System.out.println("⚠️  PLEASE CHANGE THIS PASSWORD IMMEDIATELY!");
                System.out.println("==========================================");
            }
        };
    }
}
