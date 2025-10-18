package com.example.communityforum.mapper;

import com.example.communityforum.dto.post.*;
import com.example.communityforum.dto.user.UserResponseDTO;
import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.LikeRepository;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {
    private final LikeRepository likeRepository;

    public PostMapper(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    public PostListResponseDTO toListDTO(Post post, User currentUser) {
        long likeCount = likeRepository.countByPostId(post.getId());
        boolean liked = currentUser != null && likeRepository.existsByUserAndPost(currentUser, post);

        return PostListResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .username(post.getUser() != null ? post.getUser().getUsername() : null)
                .likeCount(likeCount)
                .liked(liked)
                .build();
    }

    public PostDetailResponseDTO toDetailDTO(Post post, User currentUser) {
        long likeCount = likeRepository.countByPostId(post.getId());
        boolean liked = currentUser != null && likeRepository.existsByUserAndPost(currentUser, post);

        User user = post.getUser();

        return PostDetailResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .user(user != null ? UserResponseDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build() : null)
                .likeCount(likeCount)
                .liked(liked)
                .build();
    }
}
