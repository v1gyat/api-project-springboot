package com.example.apiproject.service.strategy;

import com.example.apiproject.entity.Task;
import com.example.apiproject.entity.User;
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
    public void assign(Task task, Long userId) {
        // Find user by ID, throw exception if not found
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Assign user to task
        task.setAssignedTo(user);

        // Note: Do not save here - service layer handles transaction management
    }
}
