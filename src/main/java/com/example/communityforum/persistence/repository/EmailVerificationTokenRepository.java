package com.example.communityforum.persistence.repository;

import com.example.communityforum.persistence.entity.EmailVerificationToken;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByExpiresAtBefore(java.time.LocalDateTime cutoff);

@Modifying(clearAutomatically = true, flushAutomatically = true)
@Transactional
void deleteByUser_IdAndPurpose(Long userId, String purpose);
    boolean existsByNewEmailAndPurpose(String newEmail, String purpose);
}
