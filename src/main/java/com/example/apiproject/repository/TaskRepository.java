package com.example.apiproject.repository;

import com.example.apiproject.entity.Task;
import com.example.apiproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    // --- Unpaged methods (used by strategy pattern, etc.) ---

    // Used by LeastLoadedAssignmentStrategy to count active tasks per user
    List<Task> findByAssignedTo(User user);

    // Filtered/paged queries are now handled by JpaSpecificationExecutor.findAll(spec, pageable)
    // via TaskSpecifications — see TaskServiceImpl.getAllTasks()
}
