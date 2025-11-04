package com.example.communityforum.persistence.repository;

import com.example.communityforum.persistence.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameIgnoreCase(String name);
    Optional<Tag> findByName(String name);
}
