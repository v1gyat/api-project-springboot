package com.example.apiproject.service.strategy;

import com.example.apiproject.entity.AssignmentType;
import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.Task;
import com.example.apiproject.entity.User;
import com.example.apiproject.exception.BadRequestException;
import com.example.apiproject.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * Random Assignment Strategy
 * Assigns tasks randomly to any active user with USER role
 * UserId parameter is ignored - system selects user automatically
 */
@Component
public class RandomAssignmentStrategy implements TaskAssignmentStrategy {

    private final UserRepository userRepository;
    private final Random random;

    public RandomAssignmentStrategy(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.random = new Random();
    }

    @Override
    public AssignmentType getType() {
        return AssignmentType.RANDOM;
    }

    @Override
    public void assign(Task task, Long userId) {
        // UserId is ignored for random assignment

        // Fetch all active users with USER role
        List<User> availableUsers = userRepository.findAllByRoleAndIsActiveTrue(Role.USER);

        // Validate at least one user is available
        if (availableUsers.isEmpty()) {
            throw new BadRequestException("No active users available for random assignment");
        }

        // Select random user
        User randomUser = availableUsers.get(random.nextInt(availableUsers.size()));

        // Assign task to random user
        task.setAssignedTo(randomUser);

        // Note: Do not save here - service layer handles transaction management
    }
}
