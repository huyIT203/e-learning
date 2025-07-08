package com.elearning.elearning_backend.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.elearning.elearning_backend.Model.LessonProgress;

@Repository
public interface LessonProgressRepository extends MongoRepository<LessonProgress, String> {
    int countByUserIdAndCompletedTrue(String userId);

    List<LessonProgress> findByUserIdAndCompletedTrue(String userId);

    int countByUserIdAndCourseIdAndCompletedTrue(String userId, String courseId);

    List<LessonProgress> findByUserIdAndCompletedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
    int countByUserIdAndCompletedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
    
    // Thêm các method mới cần thiết
    Optional<LessonProgress> findByUserIdAndCourseIdAndLessonId(String userId, String courseId, String lessonId);
    
    List<LessonProgress> findByUserIdAndCourseId(String userId, String courseId);
    
    List<LessonProgress> findByUserIdAndCourseIdOrderByCompletedAtDesc(String userId, String courseId);
    
    int countByUserIdAndCourseId(String userId, String courseId);
    
    // Method to delete all progress for a course
    void deleteByCourseId(String courseId);
    
    // Method to find all progress for a course (for statistics)
    List<LessonProgress> findByCourseId(String courseId);
}
