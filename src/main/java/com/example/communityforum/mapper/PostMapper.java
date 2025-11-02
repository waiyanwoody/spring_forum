package com.example.communityforum.mapper;

import com.example.communityforum.dto.post.*;
import com.example.communityforum.dto.user.AuthorDTO;
import com.example.communityforum.dto.user.UserResponseDTO;
import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.persistence.entity.Tag;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.LikeRepository;

import java.util.List;
import java.util.Map;

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

        // Get author safely
        User author = post.getUser();
        AuthorDTO authorDTO = null;
        if (author != null) {
            authorDTO = new AuthorDTO(
                    author.getId(),
                    author.getUsername(),
                    author.getAvatarPath());
        }

        return PostListResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .tags(post.getTags() != null
                        ? post.getTags().stream()
                                .map(Tag::getName)
                                .toList()
                        : List.of())
                .createdAt(post.getCreatedAt())
                .author(authorDTO) //  embedded author info
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
                .tags(post.getTags() != null
                ? post.getTags()
                    .stream()
                    .map(Tag::getName)   // extract only tag name
                    .toList()
                : List.of())
                .createdAt(post.getCreatedAt())
                .author(user != null ? UserResponseDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .avatar_path(user.getAvatarPath())
                        .build() : null)
                .likeCount(likeCount)
                .liked(liked)
                .build();


    }

    public PostSummaryDTO mapToPostSummaryDTO(Post post, Map<Long, Long> likeCountMap) {
        return PostSummaryDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .excerpt(post.getContent().length() > 100
                        ? post.getContent().substring(0, 100) + "..."
                        : post.getContent())
                .tags(post.getTags() != null
                        ? post.getTags()
                        .stream()
                        .map(Tag::getName)   // extract only tag name
                        .toList()
                        : List.of())
                .createdAt(post.getCreatedAt().toString())
                .likeCount(likeCountMap.getOrDefault(post.getId(), 0L))
                .commentCount(post.getCommentCount())
                .build();
    }
}
