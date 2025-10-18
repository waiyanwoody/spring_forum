package com.example.communityforum.service;

import com.example.communityforum.dto.post.PostListResponseDTO;
import com.example.communityforum.dto.post.PostRequestDTO;
import com.example.communityforum.dto.post.PostDetailResponseDTO;
import com.example.communityforum.exception.PermissionDeniedException;
import com.example.communityforum.exception.ResourceNotFoundException;
import com.example.communityforum.mapper.PostMapper;
import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.LikeRepository;
import com.example.communityforum.persistence.repository.PostRepository;
import com.example.communityforum.security.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final SecurityUtils securityUtils;
    private final LikeRepository  likeRepository;
    private final PostMapper  postMapper;


    public Page<PostListResponseDTO> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);

        User user = securityUtils.getCurrentUser();

        return posts.map(post -> postMapper.toListDTO(post, user));

    }

    public PostDetailResponseDTO getPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow( () -> new ResourceNotFoundException("Post",id));

        User user = securityUtils.getCurrentUser();

       return postMapper.toDetailDTO(post,user);
    }

    public PostDetailResponseDTO addPost(PostRequestDTO request) {
        User currentUser = securityUtils.getCurrentUser();
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        //ensure post has owner
        post.setUser(currentUser);
        post.setCreatedAt(LocalDateTime.now());
        postRepository.save(post);
        return postMapper.toDetailDTO(post,currentUser);
    }

    // update post
//    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwner(@postRepository.findById(#postId).orElse(null))")
    public PostDetailResponseDTO updatePost(long id, PostRequestDTO request) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));

        // security check
        securityUtils.checkOwnerOrAdmin(existingPost);

        // update only allowed fields
        existingPost.setTitle(request.getTitle());
        existingPost.setContent(request.getContent());

        postRepository.save(existingPost);

        return postMapper.toDetailDTO(existingPost,securityUtils.getCurrentUser());
    }


    //soft delete post for admin or owner
//    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwner(@postRepository.findById(#postId).orElse(null))")
    @Transactional
    public void softDeletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        User currentUser = securityUtils.getCurrentUser();

        // security check
        securityUtils.checkOwnerOrAdmin(post);

        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(currentUser);
        postRepository.save(post);
    }

    // restore soft-deleted post
//    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwner(@postRepository.findById(#postId).orElse(null))")
    @Transactional
    public PostDetailResponseDTO restorePost(Long postId) {

        Post post = postRepository.findDeletedById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Soft-deleted Post", postId));

        User currentUser = securityUtils.getCurrentUser();

        // security check
        securityUtils.checkOwnerOrAdmin(post);

        post.setDeletedAt(null);
        post.setDeletedBy(null);
        postRepository.save(post);

        return postMapper.toDetailDTO(post,currentUser);
    }

    // hard delete post
//    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwner(@postRepository.findById(#postId).orElse(null))")
    @Transactional
    public void hardDeletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        // security check
        securityUtils.checkOwnerOrAdmin(post);

        // permanent removal
        postRepository.deleteById(postId);
    }
}
