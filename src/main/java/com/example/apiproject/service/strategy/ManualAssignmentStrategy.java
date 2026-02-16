package com.example.apiproject.service.strategy;

import com.example.apiproject.entity.AssignmentType;
import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.Task;
import com.example.apiproject.entity.User;
import com.example.apiproject.exception.BadRequestException;
import com.example.apiproject.exception.ResourceNotFoundException;
import com.example.apiproject.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class ManualAssignmentStrategy implements TaskAssignmentStrategy {

    private final UserRepository userRepository;

    // Constructor Injection
    public ManualAssignmentStrategy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AssignmentType getType() {
        return AssignmentType.MANUAL;
    }

    @Override
    public void assign(Task task, Long userId) {
        // Validate userId is provided for manual assignment
        if (userId == null) {
            throw new BadRequestException("UserId is required for MANUAL assignment");
        }

        // Find user by ID, throw exception if not found
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate user has correct role
        if (user.getRole() != Role.USER) {
            throw new BadRequestException("Tasks can only be assigned to users with USER role");
        }

        // Assign user to task
        task.setAssignedTo(user);

        // Note: Do not save here - service layer handles transaction management
    }
}
