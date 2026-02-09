package com.example.apiproject.dto;

import com.example.apiproject.entity.TaskPriority;
import com.example.apiproject.entity.TaskStatus;
import lombok.Data;

@Data
public class TaskUpdateDTO {

    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
}
