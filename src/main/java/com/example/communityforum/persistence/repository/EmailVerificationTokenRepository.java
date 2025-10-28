package com.example.communityforum.persistence.repository;

import com.example.communityforum.persistence.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByExpiresAtBefore(java.time.LocalDateTime cutoff);
}
