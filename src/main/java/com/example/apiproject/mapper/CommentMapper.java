package com.example.apiproject.mapper;

import com.example.apiproject.dto.CommentResponseDTO;
import com.example.apiproject.entity.Comment;
import org.springframework.stereotype.Component;

/**
 * Mapper component for converting Comment entities to CommentResponseDTO.
 * Maps commentedBy.id to commentedById and commentedBy.name to commentedByName.
 */
@Component
public class CommentMapper {

    /**
     * Maps Comment entity to CommentResponseDTO
     */
    public CommentResponseDTO toResponseDTO(Comment comment) {
        CommentResponseDTO response = new CommentResponseDTO();
        response.setId(comment.getId());
        response.setMessage(comment.getMessage());
        response.setCommentedById(comment.getCommentedBy().getId());
        response.setCommentedByName(comment.getCommentedBy().getName());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}
