package com.elearning.elearning_backend.Repository;

import com.elearning.elearning_backend.Model.UserQuizAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuizAttemptRepository extends MongoRepository<UserQuizAttempt, String> {
    
    // Tìm attempt của user cho một quiz cụ thể
    Optional<UserQuizAttempt> findByUserIdAndQuizId(String userId, String quizId);
    
    // Tìm tất cả attempts của user cho một lesson
    List<UserQuizAttempt> findByUserIdAndLessonId(String userId, String lessonId);
    
    // Tìm tất cả attempts của user
    List<UserQuizAttempt> findByUserId(String userId);
    
    // Tìm tất cả attempts cho một quiz
    List<UserQuizAttempt> findByQuizId(String quizId);
    
    // Tìm tất cả attempts đã hoàn thành của user
    List<UserQuizAttempt> findByUserIdAndCompleted(String userId, boolean completed);
    
    // Tìm attempt gần nhất của user cho một quiz
    @Query("{ 'userId': ?0, 'quizId': ?1 }")
    List<UserQuizAttempt> findByUserIdAndQuizIdOrderByCreatedAtDesc(String userId, String quizId);
    
    // Đếm số lần thử của user cho một quiz
    long countByUserIdAndQuizId(String userId, String quizId);
    
    // Tìm attempts theo lesson và completed status
    List<UserQuizAttempt> findByLessonIdAndCompleted(String lessonId, boolean completed);
    
    // Xóa tất cả attempts của một quiz
    void deleteByQuizId(String quizId);
    
    // Xóa tất cả attempts của một lesson
    void deleteByLessonId(String lessonId);
} 