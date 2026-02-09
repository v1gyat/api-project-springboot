package com.example.apiproject.repository;

import com.example.apiproject.entity.Task;
import com.example.apiproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // JpaRepository provides all basic CRUD operations
    // Additional custom query methods can be added here if needed

    /**
     * Find all tasks assigned to a specific user
     * 
     * @param user The user to filter tasks by
     * @return List of tasks assigned to the user
     */
    List<Task> findByAssignedTo(User user);

    List<Task> findByCreatedBy(User user);

}
