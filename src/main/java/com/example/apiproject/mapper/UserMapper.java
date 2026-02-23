package com.example.apiproject.mapper;

import com.example.apiproject.dto.UserAdminDTO;
import com.example.apiproject.dto.UserProfileDTO;
import com.example.apiproject.dto.UserSummaryDTO;
import com.example.apiproject.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component for converting User entities to various DTOs.
 * Centralizes all User mapping logic in one place.
 */
@Component
public class UserMapper {

    /**
     * Maps User entity to UserProfileDTO (for /me endpoint)
     * Includes role so frontend can determine sidebar links
     */
    public UserProfileDTO toProfileDTO(User user) {
        return new UserProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole());
    }

    /**
     * Maps User entity to UserAdminDTO (for admin views)
     * Includes all fields: id, name, email, role, isActive, createdAt
     */
    public UserAdminDTO toAdminDTO(User user) {
        return new UserAdminDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt());
    }

    /**
     * Maps User entity to UserSummaryDTO (for manager views)
     * Lightweight: only id, name, email for task assignment dropdown
     */
    public UserSummaryDTO toSummaryDTO(User user) {
        return new UserSummaryDTO(
                user.getId(),
                user.getName(),
                user.getEmail());
    }
}
