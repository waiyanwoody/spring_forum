package com.example.communityforum.persistence.repository;

import com.example.communityforum.persistence.entity.Notification;
import com.example.communityforum.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
}
