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
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VerificationService {
    private final EmailVerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;
    private final ApplicationEventPublisher publisher;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    @Value("${app.name:Community Forum}")
    private String appName;
    @Value("${app.email.verify.expire-hours:24}")
    private int expireHours;

    public VerificationService(EmailVerificationTokenRepository tokenRepo,
            UserRepository userRepo,
            EmailService emailService,
            ApplicationEventPublisher publisher,
            SpringTemplateEngine templateEngine
            ) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
        this.emailService = emailService;
        this.publisher = publisher;
        this.templateEngine = templateEngine;
    }

    @Transactional
    public void sendVerification(User user) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken evt = EmailVerificationToken.builder()
                .token(token)
                .purpose("EMAIL_VERIFY")
                .expiresAt(LocalDateTime.now().plusHours(expireHours))
                .used(false)
                .user(user)
                .build();
        tokenRepo.save(evt);

        String link = baseUrl + "/auth/verify-email?token=" + token;

        Context ctx = new Context();
        ctx.setVariable("username", user.getUsername());
        ctx.setVariable("verifyLink", link);
        ctx.setVariable("appName", appName);
        ctx.setVariable("expireHours", expireHours);

        String html = templateEngine.process("mail/verify-email", ctx);
        publisher.publishEvent(new EmailVerificationRequested(user.getEmail(), "Verify your email", html));
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

    @Transactional
    public void startEmailChange(User user, String newEmail, boolean unverifyUntilConfirmed) {
        // reject same or duplicate email
        if (user.getEmail().equalsIgnoreCase(newEmail)) {
            throw HttpStatusException.of("New email is the same as current", HttpStatus.BAD_REQUEST);
        }
        if (userRepo.existsByEmail(newEmail)) {
            throw HttpStatusException.of("Email is already registered", HttpStatus.BAD_REQUEST);
        }
        // optional: prevent concurrent requests to same address
        if (tokenRepo.existsByNewEmailAndPurpose(newEmail, "EMAIL_UPDATE")) {
            throw HttpStatusException.of("A verification was already sent to this email", HttpStatus.BAD_REQUEST);
        }
        // clear previous pending updates for this user
        tokenRepo.deleteByUser_IdAndPurpose(user.getId(), "EMAIL_UPDATE");

        // create token
        String token = UUID.randomUUID().toString();
        EmailVerificationToken evt = EmailVerificationToken.builder()
                .token(token)
                .purpose("EMAIL_UPDATE")
                .expiresAt(LocalDateTime.now().plusHours(expireHours))
                .used(false)
                .newEmail(newEmail)
                .user(user)
                .build();
        tokenRepo.save(evt);

        // restrict until confirm
        if (unverifyUntilConfirmed) {
            user.setEmailVerified(false);
            userRepo.save(user);
        }

        // email to NEW address
        String link = baseUrl + "/auth/confirm-email-change?token=" + token;
        Context ctx = new Context();
        ctx.setVariable("username", user.getUsername());
        ctx.setVariable("verifyLink", link);
        ctx.setVariable("appName", appName);
        ctx.setVariable("expireHours", expireHours);
        ctx.setVariable("newEmail", newEmail);
        String html = templateEngine.process("mail/confirm-email-change", ctx);
        publisher.publishEvent(new EmailVerificationRequested(newEmail, "Confirm your new email", html));
    }

    @Transactional
    public void confirmEmailChange(String token) {
        EmailVerificationToken evt = tokenRepo.findByToken(token)
                .orElseThrow(() -> HttpStatusException.of("Invalid email change token", HttpStatus.BAD_REQUEST));
        if (!"EMAIL_UPDATE".equals(evt.getPurpose())) {
            throw HttpStatusException.of("Invalid token purpose", HttpStatus.BAD_REQUEST);
        }
        if (evt.isUsed() || evt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw HttpStatusException.of("Email change token expired or already used", HttpStatus.BAD_REQUEST);
        }
        // finalize change
        User user = evt.getUser();
        String newEmail = evt.getNewEmail();
        if (newEmail == null || newEmail.isBlank()) {
            throw HttpStatusException.of("Missing new email in token", HttpStatus.BAD_REQUEST);
        }
        // last-second uniqueness guard
        if (userRepo.existsByEmail(newEmail)) {
            throw HttpStatusException.of("Email is already registered", HttpStatus.BAD_REQUEST);
        }
        user.setEmail(newEmail);
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepo.save(user);

        evt.setUsed(true);
        tokenRepo.save(evt);
    }
}