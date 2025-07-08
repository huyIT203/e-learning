package com.elearning.elearning_backend.Controller;

import com.elearning.elearning_backend.DTO.ExamSubmissionRequest;
import com.elearning.elearning_backend.Model.ExamSubmission;
import com.elearning.elearning_backend.Repository.ExamSubmissionRepository;
import com.elearning.elearning_backend.Service.ExamSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamSubmissionController {
    private final ExamSubmissionService examSubmissionService;


    @PostMapping("/{examId}/submit")
    public ResponseEntity<Double> submitExam(
            @PathVariable String examId,
            @RequestBody ExamSubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String studentId = userDetails.getUsername();
        double score = examSubmissionService.submitExam(examId, studentId, request);
        return ResponseEntity.ok(score);
    }

    @GetMapping("/{examId}/submission")
    public ResponseEntity<ExamSubmission> getStudentSubmission(
            @PathVariable String examId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String studentId = userDetails.getUsername();
        return ResponseEntity.ok(examSubmissionService.getSubmission(examId, studentId));
    }

    @GetMapping("/{examId}/submissions")
    public ResponseEntity<List<ExamSubmission>> getAllSubmissions(@PathVariable String examId) {
        return ResponseEntity.ok(examSubmissionService.getAllSubmissions(examId));
    }

    @GetMapping("/submissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExamSubmission>> getAllSubmissions() {
        return ResponseEntity.ok(examSubmissionService.getAllSubmissionsAllExams());

    }

}
