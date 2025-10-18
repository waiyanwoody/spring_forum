package com.example.communityforum.dto;

import com.example.communityforum.persistence.entity.Post;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDTO {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();
    private long likeCount;
    private boolean liked;

}
