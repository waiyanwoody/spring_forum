package com.example.communityforum.service;

import com.example.communityforum.events.EmailVerificationRequested;
import com.example.communityforum.exception.HttpStatusException;
import com.example.communityforum.mail.EmailService;
import com.example.communityforum.persistence.entity.EmailVerificationToken;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.EmailVerificationTokenRepository;
import com.example.communityforum.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VerificationService {
    private final EmailVerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;
    private final ApplicationEventPublisher publisher;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public VerificationService(EmailVerificationTokenRepository tokenRepo,
            UserRepository userRepo,
            EmailService emailService,
            ApplicationEventPublisher publisher) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
        this.emailService = emailService;
        this.publisher = publisher;
    }

    @Transactional
    public void sendVerification(User user) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken evt = EmailVerificationToken.builder()
                .token(token)
                .purpose("EMAIL_VERIFY")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .user(user)
                .build();
        tokenRepo.save(evt);

        String link = baseUrl + "/auth/verify-email?token=" + token;
        String body = "Hi " + user.getUsername() + ",\n\nPlease verify your email by clicking the link:\n" + link +
                "\n\nThis link expires in 24 hours.";
        publisher.publishEvent(new EmailVerificationRequested(
                user.getEmail(), "Verify your email", body));
    }

    @Transactional
    public void verify(String token) {
        EmailVerificationToken evt = tokenRepo.findByToken(token)
                .orElseThrow(() -> HttpStatusException.of("Invalid verification token", HttpStatus.BAD_REQUEST));

        if (evt.isUsed() || evt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw HttpStatusException.of("Verification token expired or already used", HttpStatus.BAD_REQUEST);
        }

        User user = evt.getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepo.save(user);

        evt.setUsed(true);
        tokenRepo.save(evt);
    }
}