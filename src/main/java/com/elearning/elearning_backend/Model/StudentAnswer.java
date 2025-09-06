package com.elearning.elearning_backend.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("student_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAnswer {
    @Id
    private String id;

    private String submissionId;
    private String questionId;
    private int selectedOptionIndex;
    private boolean isCorrect;
}
