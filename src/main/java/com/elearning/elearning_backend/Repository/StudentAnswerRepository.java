package com.elearning.elearning_backend.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.elearning.elearning_backend.Model.StudentAnswer;

public interface StudentAnswerRepository extends MongoRepository<StudentAnswer,String> {
    List<StudentAnswer> findBySubmissionId(String submissionId);
    
    // Method to delete all answers for a submission
    void deleteBySubmissionId(String submissionId);
}
