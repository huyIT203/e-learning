package com.elearning.elearning_backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.elearning.elearning_backend.Model.ExamSubmission;

public interface ExamSubmissionRepository extends MongoRepository<ExamSubmission,String> {
    List<ExamSubmission> findByStudentId(String studentId);
    List<ExamSubmission> findByExamId(String examId);
    Optional<ExamSubmission> findByExamIdAndStudentId(String examId, String studentId);
    
    // Method to delete all submissions for an exam
    void deleteByExamId(String examId);
}
