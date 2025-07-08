package com.elearning.elearning_backend.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.elearning.elearning_backend.Model.LessonProgress;
import com.elearning.elearning_backend.Repository.LessonProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LessonProgressService {
    private final LessonProgressRepository lessonProgressRepository;
    
    public LessonProgress markLessonCompleted(String userId, String courseId, String lessonId) {
        // Kiểm tra xem đã có progress chưa
        Optional<LessonProgress> existingProgress = lessonProgressRepository
            .findByUserIdAndCourseIdAndLessonId(userId, courseId, lessonId);
        
        if (existingProgress.isPresent()) {
            LessonProgress progress = existingProgress.get();
            if (!progress.isCompleted()) {
                progress.setCompleted(true);
                progress.setCompletedAt(LocalDateTime.now());
                return lessonProgressRepository.save(progress);
            }
            return progress;
        } else {
            // Tạo mới
            LessonProgress newProgress = LessonProgress.builder()
                .userId(userId)
                .courseId(courseId)
                .lessonId(lessonId)
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();
            return lessonProgressRepository.save(newProgress);
        }
    }
    
    public LessonProgress markLessonAccessed(String userId, String courseId, String lessonId) {
        // Kiểm tra xem đã có progress chưa
        Optional<LessonProgress> existingProgress = lessonProgressRepository
            .findByUserIdAndCourseIdAndLessonId(userId, courseId, lessonId);
        
        if (existingProgress.isPresent()) {
            // Nếu đã có progress, không cần tạo mới
            return existingProgress.get();
        } else {
            // Tạo mới progress entry mà không đánh dấu completed
            LessonProgress newProgress = LessonProgress.builder()
                .userId(userId)
                .courseId(courseId)
                .lessonId(lessonId)
                .completed(false)
                .completedAt(LocalDateTime.now()) // Thời gian truy cập
                .build();
            return lessonProgressRepository.save(newProgress);
        }
    }
    
    public boolean isLessonCompleted(String userId, String courseId, String lessonId) {
        return lessonProgressRepository.findByUserIdAndCourseIdAndLessonId(userId, courseId, lessonId)
            .map(LessonProgress::isCompleted)
            .orElse(false);
    }
    
    public List<LessonProgress> getCourseProgress(String userId, String courseId) {
        return lessonProgressRepository.findByUserIdAndCourseId(userId, courseId);
    }
    
    public String getLastAccessedLessonId(String userId, String courseId) {
        List<LessonProgress> progressList = lessonProgressRepository
            .findByUserIdAndCourseIdOrderByCompletedAtDesc(userId, courseId);
        
        if (!progressList.isEmpty()) {
            return progressList.get(0).getLessonId();
        }
        
        return null; // Chưa có lesson nào được truy cập
    }
    
    public boolean hasCourseStarted(String userId, String courseId) {
        return lessonProgressRepository.countByUserIdAndCourseId(userId, courseId) > 0;
    }
    
    public int getCompletedLessonsCount(String userId, String courseId) {
        return lessonProgressRepository.countByUserIdAndCourseIdAndCompletedTrue(userId, courseId);
    }
    
    // Statistics methods for admin/teacher
    public Map<String, Integer> getCourseCompletionStats(String courseId, int totalLessons) {
        Map<String, Integer> stats = new HashMap<>();
        
        if (totalLessons == 0) {
            stats.put("completed50", 0);
            stats.put("completed75", 0);
            stats.put("completed100", 0);
            return stats;
        }
        
        // Get all users who have progress in this course
        List<LessonProgress> allProgress = lessonProgressRepository.findByCourseId(courseId);
        
        // Group by user
        Map<String, List<LessonProgress>> progressByUser = allProgress.stream()
                .collect(Collectors.groupingBy(LessonProgress::getUserId));
        
        int completed50 = 0;
        int completed75 = 0;
        int completed100 = 0;
        
        for (Map.Entry<String, List<LessonProgress>> entry : progressByUser.entrySet()) {
            long completedCount = entry.getValue().stream()
                    .filter(LessonProgress::isCompleted)
                    .count();
            
            double completionPercentage = (double) completedCount / totalLessons * 100;
            
            if (completionPercentage >= 100) {
                completed100++;
            } else if (completionPercentage >= 75) {
                completed75++;
            } else if (completionPercentage >= 50) {
                completed50++;
            }
        }
        
        stats.put("completed50", completed50);
        stats.put("completed75", completed75);
        stats.put("completed100", completed100);
        
        return stats;
    }
    
    public Map<String, Object> getDetailedCourseStats(String courseId, int totalLessons) {
        Map<String, Object> stats = new HashMap<>();
        
        // Get completion stats
        Map<String, Integer> completionStats = getCourseCompletionStats(courseId, totalLessons);
        stats.putAll(completionStats);
        
        // Get total enrolled students
        List<LessonProgress> allProgress = lessonProgressRepository.findByCourseId(courseId);
        long totalStudents = allProgress.stream()
                .map(LessonProgress::getUserId)
                .distinct()
                .count();
        
        stats.put("totalStudents", totalStudents);
        stats.put("totalLessons", totalLessons);
        
        return stats;
    }
} 