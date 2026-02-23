package com.example.apiproject.mapper;

import com.example.apiproject.dto.TaskResponseDTO;
import com.example.apiproject.entity.Task;
import org.springframework.stereotype.Component;

/**
 * Mapper component for converting Task entities to TaskResponseDTO.
 * Handles null checks for assignedTo, createdBy, and updatedBy.
 */
@Component
public class TaskMapper {

    /**
     * Maps Task entity to TaskResponseDTO
     * Safely handles null user references for assignedTo, createdBy, updatedBy
     */
    public TaskResponseDTO toResponseDTO(Task task) {
        TaskResponseDTO response = new TaskResponseDTO();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setCreatedAt(task.getCreatedAt());

        // Map assigned user details if task is assigned
        if (task.getAssignedTo() != null) {
            response.setAssignedUserId(task.getAssignedTo().getId());
            response.setAssignedUserName(task.getAssignedTo().getName());
        }

        // Map creator details if task has a creator
        if (task.getCreatedBy() != null) {
            response.setCreatedByUserId(task.getCreatedBy().getId());
            response.setCreatedByUserName(task.getCreatedBy().getName());
        }

        // Map updatedBy details if task has been updated by someone
        if (task.getUpdatedBy() != null) {
            response.setUpdatedByUserId(task.getUpdatedBy().getId());
            response.setUpdatedByUserName(task.getUpdatedBy().getName());
        }

        return response;
    }
}
