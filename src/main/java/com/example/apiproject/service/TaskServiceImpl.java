package com.example.apiproject.service;

import com.example.apiproject.dto.TaskRequestDTO;
import com.example.apiproject.dto.TaskResponseDTO;
import com.example.apiproject.dto.TaskUpdateDTO;
import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.Task;
import com.example.apiproject.entity.TaskPriority;
import com.example.apiproject.entity.TaskStatus;
import com.example.apiproject.entity.User;
import com.example.apiproject.exception.BadRequestException;
import com.example.apiproject.exception.ForbiddenException;
import com.example.apiproject.exception.ResourceNotFoundException;
import com.example.apiproject.mapper.TaskMapper;
import com.example.apiproject.repository.TaskRepository;
import com.example.apiproject.repository.UserRepository;
import com.example.apiproject.entity.AssignmentType;
import com.example.apiproject.service.strategy.TaskAssignmentStrategyFactory;
import com.example.apiproject.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAssignmentStrategyFactory strategyFactory;
    private final TaskMapper taskMapper;
    private final SecurityUtils securityUtils;

    // Constructor Injection - Best Practice for Dependency Injection
    // Depends on factory to get the right strategy dynamically
    public TaskServiceImpl(TaskRepository taskRepository,
            UserRepository userRepository,
            TaskAssignmentStrategyFactory strategyFactory,
            TaskMapper taskMapper,
            SecurityUtils securityUtils) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.strategyFactory = strategyFactory;
        this.taskMapper = taskMapper;
        this.securityUtils = securityUtils;
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
        User creator = securityUtils.getCurrentUser();
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

        // Step 6: Convert to DTO using mapper
        return taskMapper.toResponseDTO(savedTask);
    }

    @Override
    @Transactional
    public TaskResponseDTO assignTask(Long taskId, Long userId, AssignmentType assignmentType) {
        // Step 1: Find task by ID, throw exception if not found
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Step 2: Get the appropriate strategy from factory
        // Factory pattern enables dynamic strategy selection without modifying this
        // code
        var strategy = strategyFactory.getStrategy(assignmentType);

        // Step 3: Use selected strategy to assign task
        // Each strategy handles its own validation (e.g., userId requirement)
        strategy.assign(task, userId);

        // Step 4: Save the updated task
        Task updatedTask = taskRepository.save(task);

        // Step 5: Convert to DTO and return
        return taskMapper.toResponseDTO(updatedTask);
    }

    @Override
    public TaskResponseDTO getTaskById(Long id) {
        // Step 1: Find task by ID, throw exception if not found
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        // Step 2: Get current user
        User currentUser = securityUtils.getCurrentUser();

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

        return taskMapper.toResponseDTO(task);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskUpdateDTO updateDTO) {
        // Step 1: Find the task by ID
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        // Step 2: Get current user and check role
        User currentUser = securityUtils.getCurrentUser();
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
        return taskMapper.toResponseDTO(updatedTask);
    }

    @Override
    public Page<TaskResponseDTO> getAllTasks(TaskStatus status, TaskPriority priority,
            Long assignedToUserId, Pageable pageable) {
        // Get current user
        User currentUser = securityUtils.getCurrentUser();
        Role role = currentUser.getRole();

        Page<Task> tasks;

        if (role == Role.ADMIN || role == Role.MANAGER) {
            // ADMIN and MANAGER see all tasks (with optional filters)
            tasks = taskRepository.findByFilters(status, priority, assignedToUserId, pageable);
        } else {
            // USER sees only tasks assigned to them (with optional filters)
            tasks = taskRepository.findByAssignedToAndFilters(
                    currentUser, status, priority, pageable);
        }

        // Convert Page<Task> to Page<TaskResponseDTO> using .map()
        return tasks.map(taskMapper::toResponseDTO);
    }
}
