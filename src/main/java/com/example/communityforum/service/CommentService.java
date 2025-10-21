package com.example.communityforum.service;

import com.example.communityforum.dto.comment.CommentRequestDTO;
import com.example.communityforum.dto.comment.CommentResponseDTO;
import com.example.communityforum.events.CommentCreatedEvent;
import com.example.communityforum.persistence.entity.Comment;
import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.CommentRepository;
import com.example.communityforum.persistence.repository.PostRepository;
import com.example.communityforum.persistence.repository.UserRepository;
import com.example.communityforum.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository, SecurityUtils securityUtils,  ApplicationEventPublisher publisher) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
        this.publisher = publisher;
    }

    // Get value from application.properties
    @Value("${comment.max-depth:2}")
    private int maxDepth;

    //create new comment
    public CommentResponseDTO addComment(CommentRequestDTO dto) {

        // Get current authenticated user
        User currentUser = securityUtils.getCurrentUser();

        Post post = postRepository.findById(dto.getPostId()).orElseThrow(
                () -> new RuntimeException("post not found!")
        );

        Comment parent = null;
        int depth = 1; // top-level comment = depth 1

        //for reply
        if(dto.getParentCommentId() != null) {
            parent = commentRepository.findById(dto.getParentCommentId()).orElseThrow(
                    () -> new RuntimeException("Parent comment not found")
            );

            depth = calculateDepth(parent) + 1;

            if (depth > maxDepth) {
                throw new RuntimeException("Maximum reply depth (" + maxDepth + ") reached");
            }
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
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
        return toResponse(saved);
    }

    // find root comments of post
    public List<CommentResponseDTO> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        List<Comment> comments = commentRepository.findByPostAndParentCommentIsNull(post);

        return comments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // to wrap to response dto
    private CommentResponseDTO toResponse(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getText());
        dto.setUsername(comment.getUser().getUsername());
        dto.setUserId(comment.getUser().getId());
        dto.setCreatedAt(comment.getCreatedAt());

        // nested for replies
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList()));
        }
        return dto;
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

    //get all comments
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    //get comment by ID
    public CommentResponseDTO getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException("comment not found!")
                );
        return toResponse(comment);
    }

    //get comment by post ID
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    //get comment by user ID
    public List<Comment> getCommentsByUserId(Long userId) {
        return commentRepository.findByUserId(userId);
    }

    //update comment
    public Comment updateComment(Long id,Comment newComment) {
        return commentRepository.findById(id).map(comment -> {
            comment.setText(newComment.getText());
            return commentRepository.save(comment);
        }).orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
    }

    //delete comment
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
