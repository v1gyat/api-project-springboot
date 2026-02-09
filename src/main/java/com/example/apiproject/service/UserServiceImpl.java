package com.example.apiproject.service;

import com.example.apiproject.dto.*;
import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.User;
import com.example.apiproject.exception.ResourceNotFoundException;
import com.example.apiproject.exception.UnauthorizedException;
import com.example.apiproject.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<?> getAllUsers() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.MANAGER) {
            List<User> activeUsers = userRepository.findAllByRoleAndIsActiveTrue(Role.USER);
            return activeUsers.stream()
                    .map(this::convertToSummaryDTO)
                    .collect(Collectors.toList());
        } else {
            List<User> allUsers = userRepository.findAll();
            return allUsers.stream()
                    .map(this::convertToAdminDTO)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public UserAdminDTO updateUserRole(Long id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);

        return convertToAdminDTO(updatedUser);
    }

    @Override
    public UserProfileDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return convertToProfileDTO(user);
    }

    @Override
    @Transactional
    public String updatePassword(String email, PasswordUpdateDTO passwordDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Verify current password
        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);

        return "Password updated successfully";
    }

    @Override
    @Transactional
    public UserAdminDTO toggleUserStatus(Long id, Boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsActive(isActive);
        User updatedUser = userRepository.save(user);

        return convertToAdminDTO(updatedUser);
    }

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    /**
     * Helper method to convert User entity to UserAdminDTO
     */
    private UserAdminDTO convertToAdminDTO(User user) {
        return new UserAdminDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt());
    }

    /**
     * Helper method to convert User entity to UserSummaryDTO (for MANAGER)
     */
    private UserSummaryDTO convertToSummaryDTO(User user) {
        return new UserSummaryDTO(
                user.getId(),
                user.getName(),
                user.getEmail());
    }

    /**
     * Helper method to convert User entity to UserProfileDTO
     */
    private UserProfileDTO convertToProfileDTO(User user) {
        return new UserProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail());
    }

}
