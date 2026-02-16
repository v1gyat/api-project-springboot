package com.example.apiproject.controller;

import com.example.apiproject.dto.TaskRequestDTO;
import com.example.apiproject.dto.TaskResponseDTO;
import com.example.apiproject.dto.TaskUpdateDTO;
import com.example.apiproject.entity.AssignmentType;
import com.example.apiproject.response.ApiResponse;
import com.example.apiproject.service.TaskService;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    // Constructor Injection - Best Practice
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Create a new task
     * POST /api/tasks
     * Only MANAGER role can create tasks
     * ADMIN does not participate in task execution
     * 
     * @param request TaskRequestDTO containing title, description, and priority
     * @return ResponseEntity with ApiResponse wrapper containing TaskResponseDTO
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> createTask(@Valid @RequestBody TaskRequestDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("User '{}' requested to create task: {}", auth.getName(), request.getTitle());

        // Call service to create task
        TaskResponseDTO taskResponse = taskService.createTask(request);

        // Wrap response in ApiResponse structure
        ApiResponse<TaskResponseDTO> response = new ApiResponse<>(
                true,
                "Task created successfully",
                taskResponse,
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Assign a task to a user using dynamic strategy selection
     * PUT /api/tasks/{id}/assign
     * Only MANAGER role can assign tasks
     * 
     * Strategy behavior:
     * - MANUAL: requires userId parameter (validates user exists and has USER role)
     * - RANDOM: userId parameter ignored (selects random active user)
     * - LEAST_LOADED: userId parameter ignored (selects user with fewest active
     * tasks)
     * 
     * @param id             The ID of the task to assign
     * @param userId         The ID of the user (required for MANUAL, ignored for
     *                       RANDOM/LEAST_LOADED)
     * @param assignmentType The assignment strategy to use (MANUAL, RANDOM,
     *                       LEAST_LOADED)
     * @return ResponseEntity with ApiResponse wrapper containing updated
     *         TaskResponseDTO
     */
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> assignTask(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @RequestParam AssignmentType assignmentType) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Manager '{}' requested to assign task {} using {} strategy",
                auth.getName(), id, assignmentType);

        // Call service to assign task with selected strategy
        TaskResponseDTO taskResponse = taskService.assignTask(id, userId, assignmentType);

        // Wrap response in ApiResponse structure
        ApiResponse<TaskResponseDTO> response = new ApiResponse<>(
                true,
                "Task assigned successfully using " + assignmentType + " strategy",
                taskResponse,
                null,
                LocalDateTime.now());

        // Return ResponseEntity with HTTP 200 OK
        return ResponseEntity.ok(response);
    }

    /**
     * Get all tasks with role-based filtering
     * GET /api/tasks
     * - ADMIN sees all tasks
     * - MANAGER sees all tasks (for oversight and backup)
     * - USER sees only tasks assigned to them
     * 
     * @return ResponseEntity with ApiResponse wrapper containing list of
     *         TaskResponseDTO
     */

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getAllTasks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("User '{}' requested to retrieve all tasks", auth.getName());

        List<TaskResponseDTO> tasks = taskService.getAllTasks();

        ApiResponse<List<TaskResponseDTO>> response = new ApiResponse<>(
                true,
                "Tasks retrieved successfully",
                tasks,
                null,
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single task by ID
     * GET /api/tasks/{id}
     * 
     * @param id The ID of the task to retrieve
     * @return ResponseEntity with ApiResponse wrapper containing TaskResponseDTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> getTaskById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("User '{}' requested to retrieve task {}", auth.getName(), id);

        TaskResponseDTO taskResponse = taskService.getTaskById(id);

        ApiResponse<TaskResponseDTO> response = new ApiResponse<>(
                true,
                "Task retrieved successfully",
                taskResponse,
                null,
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Update a task
     * PUT /api/tasks/{id}
     * 
     * @param id        The ID of the task to update
     * @param updateDTO TaskUpdateDTO containing fields to update
     * @return ResponseEntity with ApiResponse wrapper containing updated
     *         TaskResponseDTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> updateTask(
            @PathVariable Long id,
            @RequestBody TaskUpdateDTO updateDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("User '{}' requested to update task {}", auth.getName(), id);

        TaskResponseDTO taskResponse = taskService.updateTask(id, updateDTO);

        ApiResponse<TaskResponseDTO> response = new ApiResponse<>(
                true,
                "Task updated successfully",
                taskResponse,
                null,
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
