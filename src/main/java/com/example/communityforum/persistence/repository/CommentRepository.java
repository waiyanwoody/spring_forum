package com.example.communityforum.persistence.repository;


import com.example.communityforum.persistence.entity.Comment;
import com.example.communityforum.persistence.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
    List<Comment> findByUserId(Long commentId);

    // to find top-level (root) comment
    List<Comment> findByPostAndParentCommentIsNull(Post post);

    // Replies for a specific comment
    List<Comment> findByParentCommentId(Long parentCommentId);

}