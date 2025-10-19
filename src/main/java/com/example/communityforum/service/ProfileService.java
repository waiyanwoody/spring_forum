package com.example.communityforum.service;

import com.example.communityforum.dto.post.PostSummaryDTO;
import com.example.communityforum.dto.user.ProfileResponseDTO;
import com.example.communityforum.exception.ResourceNotFoundException;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.PostRepository;
import com.example.communityforum.persistence.repository.UserRepository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public ProfileService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    // Get current user's profile
    public ProfileResponseDTO getCurrentUserProfile(User currentUser) {
        return toProfileDTO(currentUser);
    }

    // Get profile by user ID (for admin)
    public ProfileResponseDTO getUserProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User",userId));
        return toProfileDTO(user);
    }

    private ProfileResponseDTO toProfileDTO(User user) {

        List<PostSummaryDTO> postSummaries = user.getPosts() == null ? List.of() :
                user.getPosts().stream()
                        .map(post -> PostSummaryDTO.builder()
                                .id(post.getId())
                                .title(post.getTitle())
                                .content(post.getContent()) // adjust field name
                                .createdAt(post.getCreatedAt().toString())
                                .build())
                        .collect(Collectors.toList());

        return ProfileResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarPath())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
