package com.example.communityforum.mail;

import com.example.communityforum.events.VerificationRequested;

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
    public void onVerificationRequested(VerificationRequested e) {

        System.out.println("=================================");
        System.out.println("VerificationRequested received!");
        System.out.println("To: " + e.to());
        System.out.println("Subject: " + e.subject());
        System.out.println("=================================");

        emailService.sendHtml(
                e.to(),
                e.subject(),
                e.body()
        );
    }
}