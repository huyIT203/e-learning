package com.elearning.elearning_backend.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.elearning.elearning_backend.Model.Exam;

public interface ExamRepository extends MongoRepository<Exam,String> {
    List<Exam> findByCourseId(String courseId);
    
    // Method to delete all exams for a course
    void deleteByCourseId(String courseId);
}
