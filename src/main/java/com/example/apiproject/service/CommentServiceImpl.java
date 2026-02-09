package com.example.apiproject.service;

import com.example.apiproject.dto.CommentRequestDTO;
import com.example.apiproject.dto.CommentResponseDTO;
import com.example.apiproject.entity.Comment;
import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.Task;
import com.example.apiproject.entity.User;
import com.example.apiproject.exception.BadRequestException;
import com.example.apiproject.exception.ForbiddenException;
import com.example.apiproject.exception.ResourceNotFoundException;
import com.example.apiproject.exception.UnauthorizedException;
import com.example.apiproject.repository.CommentRepository;
import com.example.apiproject.repository.TaskRepository;
import com.example.apiproject.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository,
            TaskRepository taskRepository,
            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CommentResponseDTO createComment(Long taskId, CommentRequestDTO request) {
        // Step 1: Validate that the task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Step 2: Get current user
        User currentUser = getCurrentUser();

        // Step 3: AUTHORIZATION - Verify user can see the task
        verifyTaskAccess(task, currentUser);

        // Step 4: Create the comment entity
        Comment comment = new Comment();
        comment.setMessage(request.getMessage());
        comment.setTask(task);
        comment.setCommentedBy(currentUser);

        // Step 5: Save the comment
        Comment savedComment = commentRepository.save(comment);

        // Step 6: Convert to DTO and return
        return mapToDTO(savedComment);
    }

    @Override
    public List<CommentResponseDTO> getCommentsByTaskId(Long taskId) {
        // Step 1: Verify task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Step 2: Get current user
        User currentUser = getCurrentUser();

        // Step 3: AUTHORIZATION - Verify user can see the task
        verifyTaskAccess(task, currentUser);

        // Step 4: Get all comments for the task
        List<Comment> comments = commentRepository.findByTaskId(taskId);

        // Step 5: Convert to DTOs and return
        return comments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long taskId, Long commentId) {
        // Step 1: Find comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        // Step 2: Verify comment belongs to the specified task
        if (!comment.getTask().getId().equals(taskId)) {
            throw new BadRequestException("Comment does not belong to this task");
        }

        // Step 3: Get current user
        User currentUser = getCurrentUser();

        // Step 4: AUTHORIZATION - Only author or admin can delete
        Role role = currentUser.getRole();
        boolean isAdmin = role == Role.ADMIN;
        boolean isAuthor = comment.getCommentedBy().getId().equals(currentUser.getId());

        if (!isAdmin && !isAuthor) {
            throw new ForbiddenException("Access denied: Only comment author or admin can delete");
        }

        // Step 5: Delete the comment
        commentRepository.delete(comment);
    }

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    /**
     * Verify user has access to the task
     * Reuses logic from TaskServiceImpl - Admin OR Creator OR Assignee
     */
    private void verifyTaskAccess(Task task, User user) {
        Role role = user.getRole();
        boolean isAdmin = role == Role.ADMIN;
        boolean isCreator = task.getCreatedBy() != null &&
                task.getCreatedBy().getId().equals(user.getId());
        boolean isAssignee = task.getAssignedTo() != null &&
                task.getAssignedTo().getId().equals(user.getId());

        if (!isAdmin && !isCreator && !isAssignee) {
            throw new ForbiddenException("Access denied: You don't have permission to access this task");
        }
    }

    /**
     * Helper method to convert Comment entity to CommentResponseDTO
     */
    private CommentResponseDTO mapToDTO(Comment comment) {
        CommentResponseDTO response = new CommentResponseDTO();
        response.setId(comment.getId());
        response.setMessage(comment.getMessage());
        response.setCommentedById(comment.getCommentedBy().getId());
        response.setCommentedBy(comment.getCommentedBy().getName());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}
