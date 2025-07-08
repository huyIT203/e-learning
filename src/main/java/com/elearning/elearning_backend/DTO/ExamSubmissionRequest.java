package com.elearning.elearning_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionRequest {
    private List<AnswerRequest> answers;
}
