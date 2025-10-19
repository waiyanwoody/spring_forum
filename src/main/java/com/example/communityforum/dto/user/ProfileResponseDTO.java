package com.example.communityforum.dto.user;

import com.example.communityforum.dto.post.PostSummaryDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String avatarUrl;            // public URL to serve avatar
    private List<PostSummaryDTO> posts;  // light-weight info on posts
    private LocalDateTime createdAt;
}
