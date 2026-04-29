package com.specification;

import com.example.apiproject.entity.Task;
import com.example.apiproject.entity.TaskPriority;
import com.example.apiproject.entity.TaskStatus;

import org.springframework.data.jpa.domain.Specification;

public class TaskSpecifications {

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> isAssignedTo(Long userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("assignedTo").get("id"), userId);
    }

    public static Specification<Task> hasCreatedBy(Long userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("createdBy").get("id"), userId);
    }
}