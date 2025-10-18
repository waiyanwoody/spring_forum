package com.example.communityforum.service;

import com.example.communityforum.dto.LikeRequestDTO;
import com.example.communityforum.persistence.entity.*;
import com.example.communityforum.persistence.repository.*;
import com.example.communityforum.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public LikeService(LikeRepository likeRepository,
                       PostRepository postRepository,
                       CommentRepository commentRepository,
                       UserRepository userRepository,
                       SecurityUtils securityUtils
    ) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.securityUtils =  securityUtils;
    }

    @Transactional
    public boolean toggleLike(LikeRequestDTO request) {
        User currentUser =  securityUtils.getCurrentUser();

        if (request.getTargetType() == LikeRequestDTO.TargetType.POST) {
            Post post = postRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            if (likeRepository.existsByUserAndPost(currentUser, post)) {
                likeRepository.deleteByUserAndPost(currentUser, post);
                return false;
            } else {
                likeRepository.save(Like.builder().user(currentUser).post(post).build());
                return true;
            }
        }

        if (request.getTargetType() == LikeRequestDTO.TargetType.COMMENT) {
            Comment comment = commentRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new RuntimeException("Comment not found"));
            if (likeRepository.existsByUserAndComment(currentUser, comment)) {
                likeRepository.deleteByUserAndComment(currentUser, comment);
                return false;
            } else {
                likeRepository.save(Like.builder().user(currentUser).comment(comment).build());
                return true;
            }
        }

        throw new IllegalArgumentException("Either postId or commentId must be provided");
    }

    public long getPostLikeCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return likeRepository.countByPost(post);
    }

    public long getCommentLikeCount(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        return likeRepository.countByComment(comment);
    }
}
