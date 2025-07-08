package com.elearning.elearning_backend.Repository;

import com.elearning.elearning_backend.Model.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {
    
    // Tìm tất cả quiz của một bài học
    List<Quiz> findByLessonIdOrderByPosition(String lessonId);
    
    // Tìm quiz theo lesson và position
    Quiz findByLessonIdAndPosition(String lessonId, int position);
    
    // Tìm quiz theo loại
    List<Quiz> findByLessonIdAndType(String lessonId, Quiz.QuizType type);
    
    // Đếm số quiz trong một bài học
    long countByLessonId(String lessonId);
    
    // Tìm quiz có position lớn hơn một giá trị
    @Query("{ 'lessonId': ?0, 'position': { $gt: ?1 } }")
    List<Quiz> findByLessonIdAndPositionGreaterThan(String lessonId, int position);
    
    // Xóa tất cả quiz của một bài học
    void deleteByLessonId(String lessonId);
} 