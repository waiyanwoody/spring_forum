package com.example.communityforum.api.controller;


import com.example.communityforum.dto.post.PostDetailResponseDTO;
import com.example.communityforum.dto.post.PostListResponseDTO;
import com.example.communityforum.dto.post.PostRequestDTO;
import com.example.communityforum.persistence.entity.Follow;
import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.FollowRepository;
import com.example.communityforum.persistence.repository.PostRepository;
import com.example.communityforum.persistence.repository.UserRepository;
import com.example.communityforum.service.PostService;
import com.example.communityforum.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@Tag(name = "Posts", description = "Endpoints for managing forum posts")
@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PostRepository  postRepository;

    public  PostController(PostService postService) {
        this.postService = postService;
    }

    // ────────────────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<Page<PostListResponseDTO>> getAllPosts(
            @ParameterObject
            @Parameter(description = "Pagination and sorting parameters (e.g., ?page=0&size=5&sort=createdAt,DESC)")
            @PageableDefault(page = 0, size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<PostListResponseDTO> response = postService.getAllPosts(pageable);

        return ResponseEntity.ok(response);
    }

    // get post feed of following users
    public List<Post> getFollowingFeed(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> followingUsers = followRepository.findByFollower(user)
                .stream()
                .map(Follow::getFollowing)
                .toList();

        return postRepository.findPostsByFollowing(followingUsers);
    }

    //Get one post by id
    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponseDTO> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    //Create a post
    @PostMapping
    @PreAuthorize("@securityUtils.isVerified()")
    public PostDetailResponseDTO addPost(@Valid @RequestBody PostRequestDTO request) {
        return postService.addPost(request);
    }

    //Update post by id
    @PutMapping("/{id}")
    @PreAuthorize("@securityUtils.isVerified()")
    public PostDetailResponseDTO updatePost(@PathVariable Long id, @RequestBody PostRequestDTO request) {
            return postService.updatePost(id,request);
    }

    // DELETE -> soft delete current user's or admin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.softDeletePost(id);
        return ResponseEntity.noContent().build();
    }

    // POST -> restore current user's or admin
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public PostDetailResponseDTO restorePost(@PathVariable Long id) {
        return  postService.restorePost(id);
    }

    // DELETE hard-delete current user's or admin
    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> hardDeletePost(@PathVariable Long id) {
        postService.hardDeletePost(id);
        return ResponseEntity.noContent().build();
    }

}
