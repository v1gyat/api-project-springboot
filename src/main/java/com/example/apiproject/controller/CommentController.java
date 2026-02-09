package com.example.apiproject.controller;

import com.example.apiproject.dto.CommentRequestDTO;
import com.example.apiproject.dto.CommentResponseDTO;
import com.example.apiproject.response.ApiResponse;
import com.example.apiproject.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Create a new comment for a specific task
     * Only MANAGER and USER roles can create comments (Admin cannot)
     * 
     * @param taskId  The ID of the task to comment on
     * @param request The comment request containing the message
     * @return ApiResponse containing the created comment
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<CommentResponseDTO>> createComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequestDTO request) {

        CommentResponseDTO comment = commentService.createComment(taskId, request);

        ApiResponse<CommentResponseDTO> response = ApiResponse.success(
                "Comment created successfully",
                comment);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all comments for a specific task
     * 
     * @param taskId The ID of the task
     * @return ApiResponse containing list of comments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponseDTO>>> getCommentsForTask(
            @PathVariable Long taskId) {

        List<CommentResponseDTO> comments = commentService.getCommentsByTaskId(taskId);

        ApiResponse<List<CommentResponseDTO>> response = ApiResponse.success(
                "Comments retrieved successfully",
                comments);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a comment
     * Only the comment author or admin can delete
     * 
     * @param taskId    The task ID
     * @param commentId The comment ID to delete
     * @return Success message
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId) {

        commentService.deleteComment(taskId, commentId);

        ApiResponse<String> response = ApiResponse.success(
                "Comment deleted successfully",
                null);

        return ResponseEntity.ok(response);
    }
}
