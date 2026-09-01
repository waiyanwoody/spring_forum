package com.example.communityforum.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendHtml(String to, String subject, String html) {
        try {
            System.out.println("Sending email to: " + to);

            MimeMessage msg = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(msg, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(msg);

            System.out.println("EMAIL SENT SUCCESSFULLY!");

        } catch (MessagingException e) {
            System.err.println("EMAIL FAILED!");
            e.printStackTrace();

            throw new RuntimeException("Failed to send email", e);
        }
    }
}
