package com.elearning.elearning_backend.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.elearning.elearning_backend.Model.Notification;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByToUserIdOrderByCreatedAtDesc(String toUserId);
    List<Notification> findByToUserIdIsNullOrderByCreatedAtDesc(); // For broadcast notifications
    List<Notification> findByToUserIdAndIsReadOrderByCreatedAtDesc(String toUserId, boolean isRead);
    long countByToUserIdAndIsRead(String toUserId, boolean isRead);
    long countByToUserIdIsNullAndIsRead(boolean isRead); // For broadcast notifications
}
