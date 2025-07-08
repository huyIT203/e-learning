package com.elearning.elearning_backend.Model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String senderId;
    
    @Column(nullable = false)
    private String receiverId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private String senderName;
    
    @Column(nullable = false)
    private String receiverName;
    
    @Column(nullable = false)
    private String senderRole;
    
    @Column(nullable = false)
    private String receiverRole;
    
    @Column(nullable = false)
    private String courseId; // ID của khóa học liên quan
    
    @Column(nullable = false)
    private String courseName; // Tên khóa học
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.CHAT;
    
    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
} 