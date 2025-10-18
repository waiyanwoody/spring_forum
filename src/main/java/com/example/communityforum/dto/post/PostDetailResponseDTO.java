package com.example.communityforum.dto.post;

import com.example.communityforum.dto.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetailResponseDTO {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();

    private UserResponseDTO user;

    private long likeCount;
    private boolean liked;

}
