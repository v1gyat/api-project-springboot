package com.example.apiproject.controller;

import com.example.apiproject.dto.PasswordUpdateDTO;
import com.example.apiproject.dto.UserAdminDTO;
import com.example.apiproject.dto.UserProfileDTO;
import com.example.apiproject.entity.Role;
import com.example.apiproject.response.ApiResponse;
import com.example.apiproject.response.PagedResponse;
import com.example.apiproject.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService) {
                this.userService = userService;
        }

        /**
         * Get all users (Admin and Manager) with pagination, sorting, and optional
         * filters
         * GET /api/users?page=0&size=10&sortBy=id&role=USER&isActive=true
         * - ADMIN: Returns all users with full details (role + isActive filters
         * applied)
         * - MANAGER: Returns only active USERs with limited details (filters ignored)
         */
        @GetMapping
        @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
        public ResponseEntity<ApiResponse<PagedResponse<?>>> getAllUsers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(required = false) Role role,
                        @RequestParam(required = false) Boolean isActive) {

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                log.info("'{}' requested to retrieve users (page={}, size={}, sortBy={}, role={}, isActive={})",
                                auth.getName(), page, size, sortBy, role, isActive);

                Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
                Page<?> users = userService.getAllUsers(role, isActive, pageable);

                ApiResponse<PagedResponse<?>> response = new ApiResponse<>(
                                true,
                                "Users retrieved successfully",
                                PagedResponse.from(users),
                                null,
                                LocalDateTime.now());

                return ResponseEntity.ok(response);
        }

        /**
         * Get current user's own profile
         * Any authenticated user can access
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
