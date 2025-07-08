package com.elearning.elearning_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseDataDTO {
    private String id;
    private String title;
    private String content;
    private String author;
    private Long timestamp;
    private String status;
    
    // Constructor cho dữ liệu cơ bản
    public FirebaseDataDTO(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.timestamp = System.currentTimeMillis();
        this.status = "active";
    }
} 