package com.example.apiproject.service.strategy;

import com.example.apiproject.entity.Task;

public interface TaskAssignmentStrategy {

    /**
     * Assigns a task to a user
     * 
     * @param task   The task to be assigned
     * @param userId The ID of the user to assign the task to
     * @throws com.example.apiproject.exception.ResourceNotFoundException if user
     *                                                                    not found
     */
    void assign(Task task, Long userId);
}
