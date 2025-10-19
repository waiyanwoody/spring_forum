package com.example.communityforum.api.controller;

import com.example.communityforum.dto.LikeRequestDTO;
import com.example.communityforum.service.CommentService;
import com.example.communityforum.service.LikeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Likes", description = "Endpoints for managing forum likes")
@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;
    private final CommentService commentService;

    public  LikeController(LikeService likeService, CommentService commentService) {
        this.likeService = likeService;
        this.commentService = commentService;
    }

    @PostMapping("/toggle")
    public String toggleLike(@Valid @RequestBody LikeRequestDTO request) {
        boolean liked = likeService.toggleLike(request);
        return liked ? "Liked successfully" : "Unliked successfully";
    }
}
