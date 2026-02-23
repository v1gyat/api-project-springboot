package com.example.apiproject.controller;

import com.example.apiproject.dto.CommentRequestDTO;
import com.example.apiproject.dto.CommentResponseDTO;
import com.example.apiproject.response.ApiResponse;
import com.example.apiproject.response.PagedResponse;
import com.example.apiproject.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
     * Get all comments for a specific task (with pagination and sorting)
     * GET /api/tasks/{taskId}/comments?page=0&size=10&sortBy=createdAt
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CommentResponseDTO>>> getCommentsForTask(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<CommentResponseDTO> comments = commentService.getCommentsByTaskId(taskId, pageable);

        ApiResponse<PagedResponse<CommentResponseDTO>> response = new ApiResponse<>(
                true,
                "Comments retrieved successfully",
                PagedResponse.from(comments),
                null,
                java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a comment
     * Only the comment author or admin can delete
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
