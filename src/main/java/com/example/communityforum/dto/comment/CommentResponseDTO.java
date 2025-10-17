package com.example.communityforum.dto.comment;

import com.example.communityforum.persistence.entity.Comment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponseDTO {
    private Long id;

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<CommentResponseDTO> getReplies() {
        return replies;
    }

    private String content;
    private String username;
    private Long userId;
    private LocalDateTime createdAt;
    private List<CommentResponseDTO> replies; // Recursive for nested replies

    public CommentResponseDTO() {

    }

    public CommentResponseDTO(Long id, String content, String username, Long userId, LocalDateTime createdAt, List<CommentResponseDTO> replies) {
        this.id = id;
        this.content = content;
        this.username = username;
        this.userId = userId;
        this.createdAt = createdAt;
        this.replies = replies;
    }
}
