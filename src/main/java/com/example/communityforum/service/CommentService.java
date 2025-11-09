package com.example.communityforum.service;

import com.example.communityforum.dto.comment.CommentRequestDTO;
import com.example.communityforum.dto.comment.CommentResponseDTO;
import com.example.communityforum.events.CommentCreatedEvent;
import com.example.communityforum.exception.ResourceNotFoundException;
import com.example.communityforum.mapper.CommentMapper;
import com.example.communityforum.persistence.entity.Comment;
import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.CommentRepository;
import com.example.communityforum.persistence.repository.PostRepository;
import com.example.communityforum.persistence.repository.UserRepository;
import com.example.communityforum.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final ApplicationEventPublisher  publisher;
    private final CommentMapper commentMapper;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository, CommentMapper commentMapper, SecurityUtils securityUtils,  ApplicationEventPublisher publisher) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
        this.securityUtils = securityUtils;
        this.publisher = publisher;
    }

    //get all comments
    public Page<CommentResponseDTO> getAllComments(Pageable pageable, Long postId) {
        Page<Comment> commentPage;

        if (postId != null) {
            commentPage = commentRepository.findByPostId(postId, pageable);
        } else {
            commentPage = commentRepository.findAll(pageable);
        }

        return commentPage.map(commentMapper::toResponseDTO);
    }

    // Get value from application.properties
    @Value("${comment.max-depth:2}")
    private int maxDepth;
    //create new comment
    public CommentResponseDTO addComment(CommentRequestDTO dto) {

        // Get current authenticated user
        User currentUser = securityUtils.getCurrentUser();

        Post post = postRepository.findById(dto.getPostId()).orElseThrow(
                () -> new ResourceNotFoundException("post",dto.getPostId())
        );

        Comment parent = null;
        int depth = 1; // top-level comment = depth 1

        //for reply
        if(dto.getParentCommentId() != null) {
            parent = commentRepository.findById(dto.getParentCommentId()).orElseThrow(
                    () -> new ResourceNotFoundException("Parent comment",dto.getParentCommentId())
            );

            depth = calculateDepth(parent) + 1;

            if (depth > maxDepth) {
                throw new RuntimeException("Maximum reply depth (" + maxDepth + ") reached");
            }
        }

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .post(post)
                .user(currentUser)
                .parentComment(parent)
                .build();

        Comment saved = commentRepository.save(comment);

        // publish event after comment created succsesfully
        publisher.publishEvent(CommentCreatedEvent.builder()
                .receiverId(post.getUser().getId())     // post owner is the receiver
                .senderId(currentUser.getId())          // commenter
                .postTitle(post.getTitle())     // for title of the post
                .build());

        System.out.println("receiver id: "+ post.getUser().getId());
        return commentMapper.toResponseDTO(saved);
    }

    // find root comments of post
    public List<CommentResponseDTO> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        List<Comment> comments = commentRepository.findByPostAndParentCommentIsNull(post);

        return comments.stream()
                .map(commentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }


    // calculate current depth of comment and return
    private int calculateDepth(Comment parentComment) {
        int depth = 1;
        Comment current =  parentComment;
        while(current.getParentComment() != null) {
            depth++;
            current = current.getParentComment();
        }
        return depth;
    }

    //get comment by ID
    public CommentResponseDTO getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("comment",id)
                );
        return commentMapper.toResponseDTO(comment);
    }

    //get comment by user ID
    public Page<CommentResponseDTO> getCommentsByUser(Long userId, Pageable pageable) {
        Page<Comment> commentsPage = commentRepository.findByUserId(userId, pageable);
        return commentsPage.map(commentMapper::toResponseDTO);
    }
    //update comment
    public CommentResponseDTO updateComment(Long id, CommentRequestDTO dto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));

        comment.setContent(dto.getContent()); // only allow updating content
        Comment updated = commentRepository.save(comment);

        return commentMapper.toResponseDTO(updated); // map to DTO for API response
    }

    //delete comment
    public void deleteComment(Long id) {
        // Check if the comment exists
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));

        // Permanently delete it
        commentRepository.delete(comment);
    }

}
