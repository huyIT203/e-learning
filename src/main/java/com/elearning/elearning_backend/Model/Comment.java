package com.elearning.elearning_backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_id", nullable = false)
    private String courseId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Column(name = "user_role")
    private String userRole; // STUDENT, TEACHER, ADMIN
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "rating")
    private Integer rating; // 1-5 stars, null for replies
    
    @Column(name = "parent_id")
    private Long parentId; // For replies, null for main comments
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "active")
    private Boolean active = true;
    
    // Transient field for replies (not stored in DB, loaded separately)
    @Transient
    private List<Comment> replies;
} 