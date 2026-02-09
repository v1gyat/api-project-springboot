package com.example.apiproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Profile DTO for users viewing their own profile
 * Exposes: id, name, email (no password, role, or isActive)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String name;
    private String email;
}
