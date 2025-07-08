package com.elearning.elearning_backend.Model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    private String id;
    
    private String name; // Unique identifier (e.g., "PROGRAMMING", "WEB_DEVELOPMENT")
    private String displayName; // Display name (e.g., "Programming", "Web Development")
    private String description; // Optional description
    private CategoryType type; // SKILL or ROLE
    private boolean active; // Whether the category is active
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy; // User who created this category
    
    public enum CategoryType {
        SKILL("Kỹ năng"),
        ROLE("Vai trò nghề nghiệp");
        
        private final String displayName;
        
        CategoryType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
} 