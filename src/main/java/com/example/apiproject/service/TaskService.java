package com.example.apiproject.service;

import com.example.apiproject.dto.TaskRequestDTO;
import com.example.apiproject.dto.TaskResponseDTO;
import com.example.apiproject.dto.TaskUpdateDTO;

import java.util.List;

public interface TaskService {

    /**
     * Creates a new task from the provided request DTO
     * 
     * @param request The task creation request containing title, description, and
     *                priority
     * @return The created task as a response DTO
     */
    TaskResponseDTO createTask(TaskRequestDTO request);

    /**
     * Assigns a task to a user
     * 
     * @param taskId The ID of the task to assign
     * @param userId The ID of the user to assign the task to
     * @return The updated task as a response DTO
     * @throws com.example.apiproject.exception.ResourceNotFoundException if task or
     *                                                                    user not
     *                                                                    found
     */
    TaskResponseDTO assignTask(Long taskId, Long userId);

    /**
     * Get a task by ID
     * 
     * @param id The ID of the task to retrieve
     * @return The task as a response DTO
     * @throws com.example.apiproject.exception.ResourceNotFoundException if task
     *                                                                    not found
     */
    TaskResponseDTO getTaskById(Long id);

    /**
     * Update a task's details
     * 
     * @param id        The ID of the task to update
     * @param updateDTO The update request containing title, description, status, or
     *                  priority
     * @return The updated task as a response DTO
     * @throws com.example.apiproject.exception.ResourceNotFoundException if task
     *                                                                    not found
     */
    TaskResponseDTO updateTask(Long id, TaskUpdateDTO updateDTO);

    /**
     * Get all tasks with role-based filtering
     * - ADMIN users see all tasks
     * - USER/MANAGER users see only tasks assigned to them
     * 
     * @return List of tasks based on user's role
     */
    List<TaskResponseDTO> getAllTasks();
}
