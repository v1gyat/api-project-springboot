package com.example.apiproject.repository;

import com.example.apiproject.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Find all comments for a specific task (paged)
     */
    Page<Comment> findByTaskId(Long taskId, Pageable pageable);
}
