package com.elearning.elearning_backend.Controller;

import com.elearning.elearning_backend.Model.Question;
import com.elearning.elearning_backend.Service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @PostMapping("/exams/{examId}/questions")
    public ResponseEntity<Question> addQuestion(@PathVariable String examId, @RequestBody Question question) {
        return ResponseEntity.ok(questionService.addQuestion(examId, question));
    }

    @GetMapping("/exams/{examId}/questions")
    public ResponseEntity<List<Question>> getQuestions(@PathVariable String examId) {
        return ResponseEntity.ok(questionService.getQuestionsByExamId(examId));
    }
}
