package com.example.apiproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO for Manager's user list
 * Contains only essential information needed for task assignment
 * Returned when MANAGER calls GET /api/users
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String name;
    private String email;
}
