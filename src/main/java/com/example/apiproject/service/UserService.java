package com.example.apiproject.service;

import com.example.apiproject.dto.PasswordUpdateDTO;
import com.example.apiproject.dto.UserAdminDTO;
import com.example.apiproject.dto.UserProfileDTO;
import com.example.apiproject.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /**
     * Get all users with pagination and optional filters.
     * - ADMIN: all users with full details (returns Page<UserAdminDTO>)
     * - MANAGER: only active USERs with limited details (returns
     * Page<UserSummaryDTO>)
     *
     * @param role     Optional filter by role (ADMIN view only)
     * @param isActive Optional filter by active status (ADMIN view only)
     * @param pageable Pagination and sorting parameters
     * @return Paged list of users (type varies by caller's role)
     */
    Page<?> getAllUsers(Role role, Boolean isActive, Pageable pageable);

    /**
     * Update a user's role (Admin only)
     */
    UserAdminDTO updateUserRole(Long id, Role newRole);

    /**
     * Get current user's profile
     */
    UserProfileDTO getUserProfile(String email);

    /**
     * Update user's own password
     */
    String updatePassword(String email, PasswordUpdateDTO passwordDTO);

    /**
     * Toggle user active status (Admin only)
     */
    UserAdminDTO toggleUserStatus(Long id, Boolean isActive);
}
