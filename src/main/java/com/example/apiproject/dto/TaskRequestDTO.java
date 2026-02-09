package com.example.apiproject.dto;

import com.example.apiproject.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    // Optional: User ID to assign this task to
    private Long assignedToUserId;
}
