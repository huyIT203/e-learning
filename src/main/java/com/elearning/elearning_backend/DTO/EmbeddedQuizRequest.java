package com.elearning.elearning_backend.DTO;

import com.elearning.elearning_backend.Model.Quiz;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddedQuizRequest {
    private String lessonId;
    private String title;
    private String description;
    private String type; // "MULTIPLE_CHOICE", "TRUE_FALSE", "FILL_BLANK"
    private Integer position;
    private Boolean isEmbedded;
    private List<QuestionRequest> questions;
    
    @Data
    @NoArgsConstructor 
    @AllArgsConstructor
    public static class QuestionRequest {
        private String questionText;
        private String type; // "MULTIPLE_CHOICE", "TRUE_FALSE", "FILL_BLANK"
        private List<OptionRequest> options;
        private String correctAnswer;
        private String explanation;
        private Integer points;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionRequest {
        private String text;
        private Boolean isCorrect;
    }
} 