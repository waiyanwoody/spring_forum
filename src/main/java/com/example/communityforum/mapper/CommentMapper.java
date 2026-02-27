package com.example.communityforum.mapper;

import com.example.communityforum.dto.comment.CommentResponseDTO;
import com.example.communityforum.persistence.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponseDTO toResponseDTO(Comment comment) {
        if (comment == null) return null;

        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorUsername(
                        comment.getUser() != null ? comment.getUser().getUsername() : null
                )
                .authorFullname(
                        comment.getUser() != null ? comment.getUser().getFullname() : null
                )
                .createdAt(comment.getCreatedAt())
                .postId(
                        comment.getPost() != null ? comment.getPost().getId() : null
                )
                .build();
    }
}
