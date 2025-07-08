package com.elearning.elearning_backend.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.elearning.elearning_backend.Model.Lesson;

public interface LessonRepository extends MongoRepository<Lesson, String> {
    List<Lesson> findByCourseId(String courseId);
    
    // Method to delete all lessons for a course
    void deleteByCourseId(String courseId);
}
