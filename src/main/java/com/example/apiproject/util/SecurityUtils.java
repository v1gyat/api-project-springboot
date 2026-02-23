package com.example.apiproject.util;

import com.example.apiproject.entity.User;
import com.example.apiproject.exception.UnauthorizedException;
import com.example.apiproject.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Shared security utility for retrieving the current authenticated user.
 * Single source of truth — injected into all services that need the current
 * user.
 */
@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get the currently authenticated user entity from the security context.
     *
     * @return the authenticated User entity
     * @throws UnauthorizedException if no valid authentication exists
     * @throws RuntimeException      if the authenticated user is not found in the
     *                               database
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
}
