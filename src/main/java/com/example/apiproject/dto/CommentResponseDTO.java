package com.example.apiproject.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponseDTO {

    private Long id;
    private String message;
    private Long commentedById; // User ID for authorization checks
    private String commentedBy; // Username/email of the user who commented
    private LocalDateTime createdAt;
}
