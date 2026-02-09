package com.example.apiproject.repository;

import com.example.apiproject.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Find all comments for a specific task
     * 
     * @param taskId The ID of the task
     * @return List of comments associated with the task
     */
    List<Comment> findByTaskId(Long taskId);
}
