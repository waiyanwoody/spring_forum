package com.example.communityforum.persistence.repository;

import com.example.communityforum.persistence.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p where p.id = :id and p.deletedAt is null")
    Optional<Post> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("select p from Post p where p.deletedAt is null order by p.createdAt desc")
    Page<Post> findAllNotDeleted(Pageable pageable);

    @Query(value = "SELECT * FROM posts WHERE id = :id", nativeQuery = true)
    Optional<Post> findByIdIncludeDeleted(@Param("id") Long id);

}