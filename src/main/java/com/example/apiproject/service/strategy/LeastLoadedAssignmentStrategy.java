package com.example.apiproject.service.strategy;

import com.example.apiproject.entity.AssignmentType;
import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.Task;
import com.example.apiproject.entity.TaskStatus;
import com.example.apiproject.entity.User;
import com.example.apiproject.exception.BadRequestException;
import com.example.apiproject.repository.TaskRepository;
import com.example.apiproject.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Least Loaded Assignment Strategy
 * Assigns tasks to the user with the fewest active (non-completed) tasks
 * UserId parameter is ignored - system selects user automatically
 */
@Component
public class LeastLoadedAssignmentStrategy implements TaskAssignmentStrategy {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public LeastLoadedAssignmentStrategy(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public AssignmentType getType() {
        return AssignmentType.LEAST_LOADED;
    }

    @Override
    public void assign(Task task, Long userId) {
        // UserId is ignored for least-loaded assignment

        // Fetch all active users with USER role
        List<User> availableUsers = userRepository.findAllByRoleAndIsActiveTrue(Role.USER);

        // Validate at least one user is available
        if (availableUsers.isEmpty()) {
            throw new BadRequestException("No active users available for assignment");
        }

        // Find user with least active tasks
        User leastLoadedUser = availableUsers.stream()
                .min(Comparator.comparingLong(user -> taskRepository.findByAssignedTo(user).stream()
                        .filter(t -> t.getStatus() != TaskStatus.DONE)
                        .count()))
                .orElseThrow(() -> new BadRequestException("Unable to find suitable user for assignment"));

        // Assign task to least loaded user
        task.setAssignedTo(leastLoadedUser);

        // Note: Do not save here - service layer handles transaction management
    }
}
