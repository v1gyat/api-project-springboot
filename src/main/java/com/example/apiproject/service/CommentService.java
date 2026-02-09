package com.example.apiproject.service;

import com.example.apiproject.dto.CommentRequestDTO;
import com.example.apiproject.dto.CommentResponseDTO;

import java.util.List;

public interface CommentService {

    /**
     * Create a comment for a specific task
     * 
     * @param taskId  The ID of the task to comment on
     * @param request The comment request containing the message
     * @return The created comment as a response DTO
     * @throws com.example.apiproject.exception.ResourceNotFoundException if task
     *                                                                    not
     *                                                                    found
     */
    CommentResponseDTO createComment(Long taskId, CommentRequestDTO request);

    /**
     * Get all comments for a specific task
     * 
     * @param taskId The ID of the task
     * @return List of comments for the task
     */
    List<CommentResponseDTO> getCommentsByTaskId(Long taskId);

    /**
     * Delete a comment
     * Only the comment author or admin can delete
     * 
     * @param taskId    The task ID (for validation)
     * @param commentId The comment ID to delete
     * @throws RuntimeException                                           if user is
     *                                                                    not author
     *                                                                    or admin
     * @throws com.example.apiproject.exception.ResourceNotFoundException if comment
     *                                                                    not found
     */
    void deleteComment(Long taskId, Long commentId);
}
