package com.example.communityforum.api.controller;


import com.example.communityforum.dto.post.PostDetailResponseDTO;
import com.example.communityforum.dto.post.PostListResponseDTO;
import com.example.communityforum.dto.post.PostRequestDTO;
import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.service.PostService;
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

@Tag(name = "Posts", description = "Endpoints for managing forum posts")
@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostService postService;

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

    //Get one post by id
    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponseDTO> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    //Create a post
    @PostMapping
    public PostDetailResponseDTO addPost(@Valid @RequestBody PostRequestDTO request) {
        return postService.addPost(request);
    }

    //Update post by id
    @PutMapping("/{id}")
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
