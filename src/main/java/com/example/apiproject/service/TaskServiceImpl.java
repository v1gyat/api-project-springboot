package com.example.apiproject.service;

import com.example.apiproject.dto.TaskRequestDTO;
import com.example.apiproject.dto.TaskResponseDTO;
import com.example.apiproject.dto.TaskUpdateDTO;
import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.Task;
import com.example.apiproject.entity.TaskStatus;
import com.example.apiproject.entity.User;
import com.example.apiproject.exception.BadRequestException;
import com.example.apiproject.exception.ForbiddenException;
import com.example.apiproject.exception.ResourceNotFoundException;
import com.example.apiproject.exception.UnauthorizedException;
import com.example.apiproject.repository.TaskRepository;
import com.example.apiproject.repository.UserRepository;
import com.example.apiproject.service.strategy.TaskAssignmentStrategy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAssignmentStrategy assignmentStrategy;

    // Constructor Injection - Best Practice for Dependency Injection
    // Depends on interface (TaskAssignmentStrategy) not concrete class - Loose
    // Coupling!
    public TaskServiceImpl(TaskRepository taskRepository,
            UserRepository userRepository,
            TaskAssignmentStrategy assignmentStrategy) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.assignmentStrategy = assignmentStrategy;
    }

    @Override
    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO request) {
        // Step 1: Manual mapping from DTO to Entity
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());

        // Step 2: Set default values
        task.setStatus(TaskStatus.OPEN);

        // Step 3: Get the currently logged-in user(manager)
        User creator = getCurrentUser();
        task.setCreatedBy(creator);

        // Step 4: If assignedToUserId is provided, find and assign the user
        if (request.getAssignedToUserId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignedToUserId()));
            if (assignedUser.getRole() != Role.USER) {
                throw new BadRequestException("Tasks can only be assigned to a user");
            }
            task.setAssignedTo(assignedUser);
        }

        // Note: createdAt and updatedAt are set automatically by @PrePersist

        // Step 5: Save the entity using repository
        Task savedTask = taskRepository.save(task);

        // Step 6: Convert to DTO using helper method
        return mapToDTO(savedTask);
    }

    @Override
    @Transactional
    public TaskResponseDTO assignTask(Long taskId, Long userId) {
        // Step 1: Find task by ID, throw exception if not found
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        User assignedUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (assignedUser.getRole() != Role.USER) {
            throw new BadRequestException("Tasks can only be assigned to a user");
        }
        // Step 2: Use strategy to assign task to user
        assignmentStrategy.assign(task, userId);

        // Step 3: Save the updated task
        Task updatedTask = taskRepository.save(task);

        // Step 4: Convert to DTO and return
        return mapToDTO(updatedTask);
    }

    @Override
    public TaskResponseDTO getTaskById(Long id) {
        // Step 1: Find task by ID, throw exception if not found
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        // Step 2: Get current user
        User currentUser = getCurrentUser();

        // Step 3: Authorization check based on role
        Role role = currentUser.getRole();

        if (role == Role.ADMIN || role == Role.MANAGER) {
            // Admin can view any task
            // Manager can view all tasks (for oversight and backup)
            // No restrictions
        } else if (role == Role.USER) {
            // User can only view tasks assigned to them
            boolean isAssignee = task.getAssignedTo() != null
                    && task.getAssignedTo().getId().equals(currentUser.getId());
            if (!isAssignee) {
                throw new ForbiddenException("Access denied: Users can only view tasks assigned to them");
            }
        }

        return mapToDTO(task);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskUpdateDTO updateDTO) {
        // Step 1: Find the task by ID
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        // Step 2: Get current user and check role
        User currentUser = getCurrentUser();
        Role role = currentUser.getRole();

        // Step 3: Role-based update logic
        if (role == Role.ADMIN) {
            // admin does not manage any tasks
            throw new ForbiddenException("Access denied: Admin cannot update tasks");
        } else if (role == Role.MANAGER) {
            // Manager can update all tasks (for oversight and backup)
            // No creator-only restriction
            if (updateDTO.getTitle() != null) {
                task.setTitle(updateDTO.getTitle());
            }
            if (updateDTO.getDescription() != null) {
                task.setDescription(updateDTO.getDescription());
            }
            if (updateDTO.getStatus() != null) {
                task.setStatus(updateDTO.getStatus());
            }
            if (updateDTO.getPriority() != null) {
                task.setPriority(updateDTO.getPriority());
            }
            // Track who made the update
            task.setUpdatedBy(currentUser);
        } else if (role == Role.USER) {
            // Regular users can ONLY update status of tasks assigned to them
            boolean isAssignee = task.getAssignedTo() != null
                    && task.getAssignedTo().getId().equals(currentUser.getId());

            if (!isAssignee) {
                throw new ForbiddenException("Access denied: Users can only update tasks assigned to them");
            }

            if (updateDTO.getStatus() != null) {
                task.setStatus(updateDTO.getStatus());
                task.setUpdatedBy(currentUser);
            }
            // If user tries to update other fields, throw exception
            if (updateDTO.getTitle() != null || updateDTO.getDescription() != null || updateDTO.getPriority() != null) {
                throw new ForbiddenException("Access denied: Users can only update task status");
            }
        }

        // Step 4: Save the updated task (updatedAt will be set automatically by
        // @PreUpdate)
        Task updatedTask = taskRepository.save(task);

        // Step 5: Convert to DTO and return
        return mapToDTO(updatedTask);
    }

    @Override
    public List<TaskResponseDTO> getAllTasks() {
        // Get current user
        User currentUser = getCurrentUser();

        // Check the user's role
        List<Task> tasks;
        Role role = currentUser.getRole();

        if (role == Role.ADMIN || role == Role.MANAGER) {
            // ADMIN and MANAGER sees ALL tasks
            tasks = taskRepository.findAll();
        } else if (role == Role.USER) {
            // USER sees only tasks assigned to them
            tasks = taskRepository.findByAssignedTo(currentUser);
        } else {
            tasks = new java.util.ArrayList<>();
        }

        // Convert to DTOs and return
        return tasks.stream()
                .map(this::mapToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    /**
     * Helper method to convert Task entity to TaskResponseDTO
     * Reduces code duplication and follows DRY principle
     */
    private TaskResponseDTO mapToDTO(Task task) {
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
