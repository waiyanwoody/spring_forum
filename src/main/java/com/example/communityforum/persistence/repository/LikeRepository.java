package com.example.communityforum.persistence.repository;

import com.example.communityforum.persistence.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserAndPost(User user, Post post);
    boolean existsByUserAndComment(User user, Comment comment);

    void deleteByUserAndPost(User user, Post post);
    void deleteByUserAndComment(User user, Comment comment);

    long countByPost(Post post);
    long countByComment(Comment comment);
}
