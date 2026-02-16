package com.example.apiproject.service.strategy;

import com.example.apiproject.entity.AssignmentType;
import com.example.apiproject.entity.Task;

public interface TaskAssignmentStrategy {

    /**
     * Returns the type of assignment strategy this implementation handles
     * Used by factory for dynamic strategy resolution
     * 
     * @return AssignmentType enum value
     */
    AssignmentType getType();

    /**
     * Assigns a task to a user
     * 
     * @param task   The task to be assigned
     * @param userId The ID of the user to assign the task to (may be null for some
     *               strategies)
     * @throws com.example.apiproject.exception.ResourceNotFoundException if user
     *                                                                    not found
     * @throws com.example.apiproject.exception.BadRequestException       if userId
     *                                                                    is
     *                                                                    required
     *                                                                    but not
     *                                                                    provided
     */
    void assign(Task task, Long userId);
}
