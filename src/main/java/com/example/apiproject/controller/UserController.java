package com.example.apiproject.controller;

import com.example.apiproject.dto.*;
import com.example.apiproject.entity.Role;
import com.example.apiproject.response.ApiResponse;
import com.example.apiproject.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users (Admin and Manager)
     * - ADMIN: Returns all users as List<UserAdminDTO> (full details)
     * - MANAGER: Returns only active USER role as List<UserSummaryDTO> (id, name,
     * email)
     * 
     * @return List of users (DTO type depends on caller's role)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<?>>> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("'{}' requested to retrieve all users", auth.getName());

        List<?> users = userService.getAllUsers();

        ApiResponse<List<?>> response = new ApiResponse<>(
                true,
                "Users retrieved successfully",
                users,
                null,
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Get current user's own profile
     * Any authenticated user can access
     * 
     * @return Current user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.info("User '{}' requested their own profile", email);

        UserProfileDTO profile = userService.getUserProfile(email);

        ApiResponse<UserProfileDTO> response = new ApiResponse<>(
                true,
                "Profile retrieved successfully",
                profile,
                null,
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Update current user's password
     * Any authenticated user can access
     * 
     * @param passwordDTO Contains current and new password
     * @return Success message
     */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<String>> updatePassword(@Valid @RequestBody PasswordUpdateDTO passwordDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.info("User '{}' requested to update their password", email);

        String message = userService.updatePassword(email, passwordDTO);

        ApiResponse<String> response = new ApiResponse<>(
                true,
                message,
                null,
                null,
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Update a user's role (Admin only)
     * 
     * @param id      The user ID
     * @param newRole The new role to assign
     * @return Updated user with admin-level details
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserAdminDTO>> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role newRole) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Admin '{}' requested to update role for {} to {}", auth.getName(), id, newRole);

        UserAdminDTO updatedUser = userService.updateUserRole(id, newRole);

        ApiResponse<UserAdminDTO> response = new ApiResponse<>(
                true,
                "User role updated successfully",
                updatedUser,
                null,
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Toggle user active status (Admin only)
     * 
     * @param id       The user ID
     * @param isActive New active status
     * @return Updated user with admin-level details
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserAdminDTO>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Admin '{}' requested to set user {} status to {}", auth.getName(), id,
                isActive ? "active" : "inactive");

        UserAdminDTO updatedUser = userService.toggleUserStatus(id, isActive);

        String statusMessage = isActive ? "User activated successfully" : "User deactivated successfully";

        ApiResponse<UserAdminDTO> response = new ApiResponse<>(
                true,
                statusMessage,
                updatedUser,
                null,
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
