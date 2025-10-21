package com.example.communityforum.notification;

import com.example.communityforum.dto.notification.NotificationResponseDTO;
import com.example.communityforum.events.CommentCreatedEvent;
import com.example.communityforum.persistence.entity.Notification;
import com.example.communityforum.persistence.repository.NotificationRepository;
import com.example.communityforum.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @Async
    @EventListener
    @Transactional
    public void handleCommentCreated(CommentCreatedEvent event) {
        try {
            // Validate receiver exists
            if (!userRepository.existsById(event.getReceiverId())) {
                log.warn("Receiver user not found with ID: {}", event.getReceiverId());
                return;
            }

            // Validate sender exists
            if (!userRepository.existsById(event.getSenderId())) {
                log.warn("Sender user not found with ID: {}", event.getSenderId());
                return;
            }

            // 1️⃣ Save notification to DB
            Notification notification = Notification.builder()
                    .receiverId(event.getReceiverId())
                    .senderId(event.getSenderId())
                    .type("COMMENT")
                    .message("Your post '" + event.getPostTitle() + "' got a new comment.")
                    .read(false)
                    .build();

            Notification saved = notificationRepository.save(notification);

            // 2️⃣ Prepare DTO for client
            NotificationResponseDTO dto = new NotificationResponseDTO(
                    saved.getId(),
                    saved.getMessage(),
                    saved.getType(),
                    saved.getCreatedAt().toString()
            );

            // 3️⃣ Send to specific user's notification channel
            String destination = "/topic/user/" + event.getReceiverId() + "/notifications";
            messagingTemplate.convertAndSend(destination, dto);

            log.info("✅ Notification sent to user {} via WebSocket", event.getReceiverId());

        } catch (Exception e) {
            log.error("❌ Failed to process notification for event: {}", event, e);
        }
    }
}