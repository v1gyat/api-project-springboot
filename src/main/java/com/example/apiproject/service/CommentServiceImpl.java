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
import com.example.apiproject.mapper.CommentMapper;
import com.example.apiproject.repository.CommentRepository;
import com.example.apiproject.repository.TaskRepository;
import com.example.apiproject.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final CommentMapper commentMapper;
    private final SecurityUtils securityUtils;

    public CommentServiceImpl(CommentRepository commentRepository,
            TaskRepository taskRepository,
            CommentMapper commentMapper,
            SecurityUtils securityUtils) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.commentMapper = commentMapper;
        this.securityUtils = securityUtils;
    }

    @Override
    @Transactional
    public CommentResponseDTO createComment(Long taskId, CommentRequestDTO request) {
        // Step 1: Validate that the task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Step 2: Get current user
        User currentUser = securityUtils.getCurrentUser();

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
        return commentMapper.toResponseDTO(savedComment);
    }

    @Override
    public Page<CommentResponseDTO> getCommentsByTaskId(Long taskId, Pageable pageable) {
        // Step 1: Verify task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Step 2: Get current user
        User currentUser = securityUtils.getCurrentUser();

        // Step 3: AUTHORIZATION - Verify user can see the task
        verifyTaskAccess(task, currentUser);

        // Step 4: Get paged comments for the task
        Page<Comment> comments = commentRepository.findByTaskId(taskId, pageable);

        // Step 5: Convert Page<Comment> to Page<CommentResponseDTO> using .map()
        return comments.map(commentMapper::toResponseDTO);
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
        User currentUser = securityUtils.getCurrentUser();

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
}
