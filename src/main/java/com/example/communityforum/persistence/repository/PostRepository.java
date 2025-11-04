package com.example.communityforum.persistence.repository;

import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.persistence.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByUser_Id(Long userId, Pageable pageable);

    @Query("select p from Post p where p.id = :id and p.deletedAt is null")
    Optional<Post> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("select p from Post p where p.deletedAt is null order by p.createdAt desc")
    Page<Post> findAllNotDeleted(Pageable pageable);

    @Query(value = "SELECT * FROM posts WHERE id = :id AND deleted_at IS NOT NULL", nativeQuery = true)
    Optional<Post> findDeletedById(@Param("id") Long id);

    // find post by user that current user is following
    @Query("SELECT p FROM Post p WHERE p.user IN :followingUsers ORDER BY p.createdAt DESC")
    List<Post> findPostsByFollowing(@Param("followingUsers") List<User> followingUsers);

    boolean existsBySlug(String slug);

}