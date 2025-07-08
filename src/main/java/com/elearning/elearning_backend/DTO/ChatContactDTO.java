package com.elearning.elearning_backend.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatContactDTO {
    private String userId;
    private String userName;
    private String userRole;
    private String courseId;
    private String courseName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
    private Boolean isOnline;
    private String avatarUrl;
} 