package com.example.communityforum.service;

import com.example.communityforum.exception.PermissionDeniedException;
import com.example.communityforum.exception.ResourceNotFoundException;
import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.PostRepository;
import com.example.communityforum.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final SecurityUtils securityUtils;

    public PostService(PostRepository postRepository, SecurityUtils securityUtils) {
        this.postRepository = postRepository;
        this.securityUtils = securityUtils;
    }

    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public boolean deletePost(Long id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Post updatePost(long id, Post post) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));
        existingPost.setTitle(post.getTitle());
        existingPost.setContent(post.getContent());
        existingPost.setUser(post.getUser());
        return postRepository.save(existingPost);
    }

    public Post addPost(Post post) {
        return postRepository.save(post); // ID auto-generated
    }

    //soft delete post for admin or owner
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwner(@postRepository.findById(#postId).orElse(null))")
    @Transactional
    public void softDeletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        User currentUser = securityUtils.getCurrentUser();
        boolean isOwner = post.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new PermissionDeniedException("You don't have permission to delete this post");
        }

        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(currentUser);
        postRepository.save(post);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwner(@postRepository.findById(#postId).orElse(null))")
    @Transactional
    public void restorePost(Long postId) {
        Post post = postRepository.findByIdIncludeDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        post.setDeletedAt(null);
        post.setDeletedBy(null);
        postRepository.save(post);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwner(@postRepository.findById(#postId).orElse(null))")
    @Transactional
    public void hardDeletePost(Long postId) {
        // permanent removal (admin only)
        postRepository.deleteById(postId);
    }
}
