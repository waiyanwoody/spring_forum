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
            if (!userRepository.existsById(event.getReceiverId())) return;
            if (!userRepository.existsById(event.getSenderId())) return;

            Notification notification = Notification.builder()
                    .receiverId(event.getReceiverId())
                    .senderId(event.getSenderId())
                    .type("COMMENT")
                    .message("Your post '" + event.getPostTitle() + "' got a new comment.")
                    .read(false)
                    .build();
            Notification saved = notificationRepository.save(notification);

            NotificationResponseDTO dto = new NotificationResponseDTO(
                    saved.getId(),
                    saved.getMessage(),
                    saved.getType(),
                    saved.getCreatedAt().toString()
            );

            String username = userRepository.findById(event.getReceiverId())
                    .map(u -> u.getUsername())
                    .orElse(null);
            if (username != null) {
                // User-destination: client subscribes to /user/queue/notifications
                messagingTemplate.convertAndSendToUser(username, "/queue/notifications", dto);
                log.info("Notification sent to user {} via /user/queue/notifications", username);
            }
        } catch (Exception e) {
            log.error("Failed to process notification", e);
        }
    }
}