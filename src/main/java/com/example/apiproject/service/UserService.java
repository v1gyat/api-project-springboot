package com.example.apiproject.service;

import com.example.apiproject.dto.*;
import com.example.apiproject.entity.Role;

import java.util.List;

public interface UserService {

    /**
     * Get all users in the system
     * - ADMIN: Returns all users as List<UserAdminDTO>
     * - MANAGER: Returns only active USER role as List<UserSummaryDTO>
     * 
     * @return List of users (DTO type depends on caller's role)
     */
    List<?> getAllUsers();

    /**
     * Update a user's role (Admin only)
     * 
     * @param id      The user ID
     * @param newRole The new role to assign
     * @return Updated user as UserAdminDTO
     */
    UserAdminDTO updateUserRole(Long id, Role newRole);

    /**
     * Get current user's profile
     * 
     * @param email The email of the current user
     * @return User profile DTO
     */
    UserProfileDTO getUserProfile(String email);

    /**
     * Update user's own password
     * 
     * @param email       Current user's email
     * @param passwordDTO DTO containing current and new password
     * @return Success message
     */
    String updatePassword(String email, PasswordUpdateDTO passwordDTO);

    /**
     * Toggle user active status (Admin only)
     * 
     * @param id       User ID
     * @param isActive New active status
     * @return Updated user as UserAdminDTO
     */
    UserAdminDTO toggleUserStatus(Long id, Boolean isActive);
}
