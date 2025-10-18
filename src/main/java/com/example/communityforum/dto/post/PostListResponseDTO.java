package com.example.communityforum.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostListResponseDTO {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();

    private String username;

    private long likeCount;
    private boolean liked;

}
