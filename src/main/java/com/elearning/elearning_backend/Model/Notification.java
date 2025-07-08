package com.elearning.elearning_backend.Model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    private String id;
    private String title;
    private String message;
    private String fromUserId; // Admin ID
    private String toUserId;   // Teacher ID (null for all teachers)
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String priority; // HIGH, MEDIUM, LOW
    private String type;     // ANNOUNCEMENT, REMINDER, WARNING
    
    // File attachment fields
    private List<String> attachmentUrls; // URLs of attached files
    private List<String> attachmentNames; // Original names of attached files
    private List<String> attachmentTypes; // MIME types of attached files
    private List<Long> attachmentSizes; // File sizes in bytes
    
    public Notification(String title, String message, String fromUserId, String toUserId, String priority, String type) {
        this.title = title;
        this.message = message;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.priority = priority;
        this.type = type;
    }
    
    // Constructor with attachments
    public Notification(String title, String message, String fromUserId, String toUserId, String priority, String type,
                       List<String> attachmentUrls, List<String> attachmentNames, List<String> attachmentTypes, List<Long> attachmentSizes) {
        this.title = title;
        this.message = message;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.priority = priority;
        this.type = type;
        this.attachmentUrls = attachmentUrls;
        this.attachmentNames = attachmentNames;
        this.attachmentTypes = attachmentTypes;
        this.attachmentSizes = attachmentSizes;
    }
}
