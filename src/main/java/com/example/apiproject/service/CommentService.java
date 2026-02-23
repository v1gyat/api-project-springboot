package com.example.apiproject.service;

import com.example.apiproject.dto.CommentRequestDTO;
import com.example.apiproject.dto.CommentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    /**
     * Create a comment for a specific task
     */
    CommentResponseDTO createComment(Long taskId, CommentRequestDTO request);

    /**
     * Get all comments for a specific task with pagination
     *
     * @param taskId   The ID of the task
     * @param pageable Pagination and sorting parameters
     * @return Paged list of comments
     */
    Page<CommentResponseDTO> getCommentsByTaskId(Long taskId, Pageable pageable);

    /**
     * Delete a comment (only author or admin)
     */
    void deleteComment(Long taskId, Long commentId);
}
