package com.example.communityforum.api.controller;

import com.example.communityforum.dto.post.PostDetailResponseDTO;
import com.example.communityforum.persistence.entity.Comment;
import com.example.communityforum.service.UserActivityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Tag(
        name = "User Activity",
        description = "Endpoints for user activity"
)
public class UserActivityController {

    private final UserActivityService userActivityService;

    public UserActivityController(
            UserActivityService userActivityService
    ) {
        this.userActivityService = userActivityService;
    }

    @GetMapping("/{userId}/liked-posts")
    public List<PostDetailResponseDTO> getLikedPosts(
            @PathVariable Long userId
    ) {
        return userActivityService.getLikedPosts(userId);
    }

    @GetMapping("/{userId}/replies")
    public List<Comment> getReplies(
            @PathVariable Long userId
    ) {
        return userActivityService.getReplies(userId);
    }
}