package com.example.apiproject.dto;

import com.example.apiproject.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Admin DTO for viewing all user details (admin only)
 * Exposes: id, name, email, role, isActive, createdAt (no password)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
