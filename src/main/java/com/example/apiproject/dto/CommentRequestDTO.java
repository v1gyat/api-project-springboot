package com.example.apiproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequestDTO {

    @NotBlank(message = "Comment message is required")
    private String message;
}
