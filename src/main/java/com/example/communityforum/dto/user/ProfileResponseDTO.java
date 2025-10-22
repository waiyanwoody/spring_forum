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

    public ProfileResponseDTO(Long id, String username, String email, String bio, String avatarUrl, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }
}
