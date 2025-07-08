package com.elearning.elearning_backend.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("exam_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSubmission {
    @Id
    private String id;
    private String examId;
    private String studentId;
    private double score;
    private LocalDateTime submittedAt;
}
