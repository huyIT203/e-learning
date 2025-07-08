package com.elearning.elearning_backend.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private String senderId;
    private String receiverId;
    private String content;
    private String senderName;
    private String receiverName;
    private String senderRole;
    private String receiverRole;
    private String courseId;
    private String courseName;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private MessageType type;
    
    public enum MessageType {
        CHAT, JOIN, LEAVE, TYPING, STOP_TYPING
    }
} 