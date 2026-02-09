package com.example.apiproject.dto;

import com.example.apiproject.entity.TaskPriority;
import com.example.apiproject.entity.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime createdAt;
    private Long assignedUserId;
    private String assignedUserName;
    private Long createdByUserId;
    private String createdByUserName;
    private Long updatedByUserId;
    private String updatedByUserName;
}
