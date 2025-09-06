package com.elearning.elearning_backend.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "lesson_progress")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LessonProgress {
    @Id
    private String id;

    private String userId;
    private String courseId;
    private String lessonId;

    private boolean completed;

    @Builder.Default
    private LocalDateTime completedAt = LocalDateTime.now();
}
