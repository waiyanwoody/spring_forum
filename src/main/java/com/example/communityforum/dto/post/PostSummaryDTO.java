package com.example.communityforum.dto.post;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostSummaryDTO {
    private Long id;
    private String title;
    private String content;
    private String createdAt;
}
