package com.example.apiproject.dto;

/**
 * Lightweight DTO for Manager's user list
 * Contains only essential information needed for task assignment
 * Returned when MANAGER calls GET /api/users
 */
public record UserSummaryDTO(
        Long id,
        String name,
        String email) {
}
