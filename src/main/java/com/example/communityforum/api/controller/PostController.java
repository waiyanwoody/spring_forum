package com.example.communityforum.api.controller;

import com.example.communityforum.dto.PostResponseDTO;
import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.service.PostService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostService postService;

    public  PostController(PostService postService) {
        this.postService = postService;
    }

    //Get all Posts
    @GetMapping
    public ResponseEntity<Page<PostResponseDTO>> getAllPosts(
            @PageableDefault(page = 0, size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Post> page =  postService.getAllPosts(pageable);

        // Convert Entity -> DTO
        Page<PostResponseDTO> dtoPage = page.map(post ->
                new PostResponseDTO(post.getId(), post.getTitle(), post.getContent(), post.getCreatedAt())
        );

        return ResponseEntity.ok(dtoPage);
    }

    //Get one post by id
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return postService.getPostById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    //Create a post
    @PostMapping
    public String addPost(@RequestBody Post post) {
        postService.addPost(post);
        return "Post added successfully";
    }

    //Update post by id
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post post) {
            Post updatedPost = postService.updatePost(id, post);
            return ResponseEntity.ok(updatedPost);
    }

    //delete post by id
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        boolean deleted = postService.deletePost(id);
        if(deleted) {
            return  ResponseEntity.ok("Post deleted successfully");
        }else  {
            return ResponseEntity.notFound().build();
        }
    }

}
