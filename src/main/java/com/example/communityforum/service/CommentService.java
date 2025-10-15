package com.example.communityforum.service;

import com.example.communityforum.persistence.entity.Comment;
import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.persistence.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    //create new comment
    public Comment addComment(Comment comment) {
        return commentRepository.save(comment);
    }

    //get all comments
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    //get comment by ID
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
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
