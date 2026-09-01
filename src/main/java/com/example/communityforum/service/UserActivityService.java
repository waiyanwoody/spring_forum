package com.example.communityforum.service;

import com.example.communityforum.dto.post.PostDetailResponseDTO;
import com.example.communityforum.persistence.entity.Comment;
import com.example.communityforum.persistence.entity.Like;
import com.example.communityforum.persistence.repository.CommentRepository;
import com.example.communityforum.persistence.repository.LikeRepository;
import com.example.communityforum.mapper.PostMapper;
import com.example.communityforum.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserActivityService {

    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostMapper postMapper;
    private final SecurityUtils securityUtils;

    public UserActivityService(
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            PostMapper postMapper,
            SecurityUtils securityUtils
    ) {
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.postMapper = postMapper;
        this.securityUtils = securityUtils;
    }

    public List<PostDetailResponseDTO> getLikedPosts(Long userId) {

        // The logged-in user is needed for the "liked" field
        var currentUser = securityUtils.getCurrentUser();

        return likeRepository
                .findByUserIdAndPostIsNotNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(Like::getPost)
                .map(post -> postMapper.toDetailDTO(post, currentUser))
                .toList();
    }

    public List<Comment> getReplies(Long userId) {

        return commentRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }
}