package com.example.communityforum.dto.post;

import com.example.communityforum.dto.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetailResponseDTO {

    private Long id;
    private String title;
    private String content;
    private List<String> tags;
    private String slug;
    private LocalDateTime createdAt;

    private UserResponseDTO author;

    private long likeCount;
    private boolean liked;
    private long commentCount;
}