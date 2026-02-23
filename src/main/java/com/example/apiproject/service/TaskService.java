package com.example.apiproject.service;

import com.example.apiproject.dto.TaskRequestDTO;
import com.example.apiproject.dto.TaskResponseDTO;
import com.example.apiproject.dto.TaskUpdateDTO;
import com.example.apiproject.entity.AssignmentType;
import com.example.apiproject.entity.TaskPriority;
import com.example.apiproject.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    /**
     * Creates a new task from the provided request DTO
     */
    TaskResponseDTO createTask(TaskRequestDTO request);

    /**
     * Assigns a task to a user using the specified assignment strategy
     */
    TaskResponseDTO assignTask(Long taskId, Long userId, AssignmentType assignmentType);

    /**
     * Get a task by ID (with role-based access control)
     */
    TaskResponseDTO getTaskById(Long id);

    /**
     * Update a task's details (with role-based field restrictions)
     */
    TaskResponseDTO updateTask(Long id, TaskUpdateDTO updateDTO);

    /**
     * Get all tasks with pagination, sorting, and optional filters.
     * Role-based filtering is applied automatically:
     * - ADMIN/MANAGER: see all tasks (with optional filters)
     * - USER: see only tasks assigned to them (with optional filters)
     *
     * @param status           Optional filter by task status
     * @param priority         Optional filter by task priority
     * @param assignedToUserId Optional filter by assigned user ID
     * @param pageable         Pagination and sorting parameters
     * @return Paged list of tasks
     */
    Page<TaskResponseDTO> getAllTasks(TaskStatus status, TaskPriority priority,
            Long assignedToUserId, Pageable pageable);
}
