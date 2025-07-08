package com.elearning.elearning_backend.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.elearning.elearning_backend.Model.Question;

public interface QuestionRepository extends MongoRepository<Question,String> {
    List<Question> findByExamId(String examId);
    
    // Method to delete all questions for an exam
    void deleteByExamId(String examId);
}
