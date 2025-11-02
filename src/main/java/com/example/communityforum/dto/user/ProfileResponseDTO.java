package com.example.communityforum.dto.user;

import com.example.communityforum.dto.post.PostSummaryDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileResponseDTO {
    private Long id;
    private String fullname;
    private String username;
    private String email;
    private boolean email_verified;
    private String bio;
    private String avatar_path;            // public URL to serve avatar
    private LocalDateTime createdAt;
}
