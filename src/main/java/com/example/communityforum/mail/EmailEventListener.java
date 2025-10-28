package com.example.communityforum.mail;

import com.example.communityforum.events.EmailVerificationRequested;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class EmailEventListener {
    private final EmailService emailService;

    @Async("appAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVerificationRequested(EmailVerificationRequested e) {
        emailService.send(e.to(), e.subject(), e.body());
    }
}