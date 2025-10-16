package com.example.communityforum.dto;

import jakarta.persistence.Column;

import java.time.LocalDateTime;

public class PostResponseDTO {

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();

    public PostResponseDTO(Long id, String title, String content, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
    }
}
