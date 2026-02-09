package com.example.apiproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for password update requests
 */
@Data
public class PasswordUpdateDTO {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;
}
