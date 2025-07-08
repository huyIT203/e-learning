package com.elearning.elearning_backend.Controller;

import com.elearning.elearning_backend.Model.Exam;
import com.elearning.elearning_backend.Service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;
    @PostMapping("/{courseId}/exams")
    public ResponseEntity<Exam> createExam(@PathVariable String courseId, @RequestBody Exam exam) {
        return ResponseEntity.ok(examService.createExam(courseId, exam));
    }

    @GetMapping("/{courseId}/exams")
    public ResponseEntity<List<Exam>> getExams(@PathVariable String courseId) {
        return ResponseEntity.ok(examService.getExamsByCourse(courseId));
    }
}
