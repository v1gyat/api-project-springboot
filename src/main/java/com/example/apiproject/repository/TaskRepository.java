package com.example.apiproject.repository;

import com.example.apiproject.entity.Task;
import com.example.apiproject.entity.TaskPriority;
import com.example.apiproject.entity.TaskStatus;
import com.example.apiproject.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // --- Unpaged methods (used by strategy pattern, etc.) ---

    List<Task> findByAssignedTo(User user);

    List<Task> findByCreatedBy(User user);

    // --- Paged + filtered query ---

    /**
     * Find tasks with optional filters. Each filter is ignored if null.
     * Used by ADMIN and MANAGER roles who can see all tasks.
     */
    @Query("SELECT t FROM Task t WHERE "
            + "(:status IS NULL OR t.status = :status) AND "
            + "(:priority IS NULL OR t.priority = :priority) AND "
            + "(:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)")
    Page<Task> findByFilters(
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("assignedToId") Long assignedToId,
            Pageable pageable);

    /**
     * Find tasks assigned to a specific user with optional filters.
     * Used by USER role who can only see their own assigned tasks.
     */
    @Query("SELECT t FROM Task t WHERE "
            + "t.assignedTo = :user AND "
            + "(:status IS NULL OR t.status = :status) AND "
            + "(:priority IS NULL OR t.priority = :priority)")
    Page<Task> findByAssignedToAndFilters(
            @Param("user") User user,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            Pageable pageable);
}
