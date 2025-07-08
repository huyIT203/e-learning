package com.elearning.elearning_backend.Service;

import com.elearning.elearning_backend.Model.Quiz;
import com.elearning.elearning_backend.Model.UserQuizAttempt;
import com.elearning.elearning_backend.Repository.UserQuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserQuizAttemptService {

    @Autowired
    private UserQuizAttemptRepository userQuizAttemptRepository;

    @Autowired
    private QuizService quizService;

    // Bắt đầu một attempt mới hoặc lấy attempt hiện tại
    public UserQuizAttempt startOrGetAttempt(String userId, String quizId, String lessonId) {
        Optional<UserQuizAttempt> existingAttempt = userQuizAttemptRepository.findByUserIdAndQuizId(userId, quizId);
        
        if (existingAttempt.isPresent() && !existingAttempt.get().isCompleted()) {
            // Trả về attempt hiện tại nếu chưa hoàn thành
            return existingAttempt.get();
        } else {
            // Tạo attempt mới
            UserQuizAttempt newAttempt = new UserQuizAttempt(userId, quizId, lessonId);
            return userQuizAttemptRepository.save(newAttempt);
        }
    }

    // Lưu câu trả lời tạm thời (auto-save)
    public UserQuizAttempt saveAnswer(String attemptId, String questionId, Object answer) {
        Optional<UserQuizAttempt> attemptOpt = userQuizAttemptRepository.findById(attemptId);
        
        if (attemptOpt.isPresent()) {
            UserQuizAttempt attempt = attemptOpt.get();
            
            if (attempt.getUserAnswers() == null) {
                attempt.setUserAnswers(new java.util.HashMap<>());
            }
            
            attempt.getUserAnswers().put(questionId, answer);
            attempt.setUpdatedAt(LocalDateTime.now());
            
            return userQuizAttemptRepository.save(attempt);
        }
        
        return null;
    }

    // Nộp bài và tính điểm
    public UserQuizAttempt submitQuiz(String attemptId, Map<String, Object> finalAnswers) {
        Optional<UserQuizAttempt> attemptOpt = userQuizAttemptRepository.findById(attemptId);
        
        if (attemptOpt.isPresent()) {
            UserQuizAttempt attempt = attemptOpt.get();
            
            // Cập nhật answers cuối cùng
            attempt.setUserAnswers(finalAnswers);
            
            // Tính điểm
            Map<String, Object> results = quizService.checkAnswer(attempt.getQuizId(), finalAnswers);
            
            attempt.setResults(results);
            attempt.setTotalQuestions((Integer) results.get("totalQuestions"));
            attempt.setCorrectAnswers((Integer) results.get("correctAnswers"));
            attempt.setPercentage((Double) results.get("percentage"));
            
            // Tính score dựa trên quiz
            Optional<Quiz> quizOpt = quizService.getQuizById(attempt.getQuizId());
            if (quizOpt.isPresent()) {
                Quiz quiz = quizOpt.get();
                int maxScore = quiz.getMaxScore() > 0 ? quiz.getMaxScore() : 100;
                attempt.setScore((int) (attempt.getPercentage() * maxScore / 100));
            }
            
            attempt.setCompleted(true);
            attempt.setCompletedAt(LocalDateTime.now());
            attempt.setUpdatedAt(LocalDateTime.now());
            
            return userQuizAttemptRepository.save(attempt);
        }
        
        return null;
    }

    // Lấy attempt theo ID
    public Optional<UserQuizAttempt> getAttemptById(String attemptId) {
        return userQuizAttemptRepository.findById(attemptId);
    }

    // Lấy attempt của user cho quiz cụ thể
    public Optional<UserQuizAttempt> getUserAttemptForQuiz(String userId, String quizId) {
        return userQuizAttemptRepository.findByUserIdAndQuizId(userId, quizId);
    }

    // Lấy tất cả attempts của user cho lesson
    public List<UserQuizAttempt> getUserAttemptsForLesson(String userId, String lessonId) {
        return userQuizAttemptRepository.findByUserIdAndLessonId(userId, lessonId);
    }

    // Lấy tất cả attempts của user
    public List<UserQuizAttempt> getUserAttempts(String userId) {
        return userQuizAttemptRepository.findByUserId(userId);
    }

    // Lấy attempts đã hoàn thành của user
    public List<UserQuizAttempt> getCompletedAttempts(String userId) {
        return userQuizAttemptRepository.findByUserIdAndCompleted(userId, true);
    }

    // Xóa attempt
    public boolean deleteAttempt(String attemptId) {
        if (userQuizAttemptRepository.existsById(attemptId)) {
            userQuizAttemptRepository.deleteById(attemptId);
            return true;
        }
        return false;
    }

    // Reset attempt (cho phép làm lại)
    public UserQuizAttempt resetAttempt(String userId, String quizId, String lessonId) {
        Optional<UserQuizAttempt> existingAttempt = userQuizAttemptRepository.findByUserIdAndQuizId(userId, quizId);
        
        if (existingAttempt.isPresent()) {
            userQuizAttemptRepository.delete(existingAttempt.get());
        }
        
        // Tạo attempt mới
        UserQuizAttempt newAttempt = new UserQuizAttempt(userId, quizId, lessonId);
        return userQuizAttemptRepository.save(newAttempt);
    }

    // Kiểm tra xem user đã hoàn thành quiz chưa
    public boolean hasUserCompletedQuiz(String userId, String quizId) {
        Optional<UserQuizAttempt> attempt = userQuizAttemptRepository.findByUserIdAndQuizId(userId, quizId);
        return attempt.isPresent() && attempt.get().isCompleted();
    }

    // Lấy thống kê attempts cho quiz
    public Map<String, Object> getQuizStats(String quizId) {
        List<UserQuizAttempt> attempts = userQuizAttemptRepository.findByQuizId(quizId);
        List<UserQuizAttempt> completedAttempts = attempts.stream()
                .filter(UserQuizAttempt::isCompleted)
                .toList();

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalAttempts", attempts.size());
        stats.put("completedAttempts", completedAttempts.size());
        
        if (!completedAttempts.isEmpty()) {
            double averageScore = completedAttempts.stream()
                    .mapToDouble(UserQuizAttempt::getPercentage)
                    .average()
                    .orElse(0.0);
            stats.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
            
            double maxScore = completedAttempts.stream()
                    .mapToDouble(UserQuizAttempt::getPercentage)
                    .max()
                    .orElse(0.0);
            stats.put("maxScore", maxScore);
            
            double minScore = completedAttempts.stream()
                    .mapToDouble(UserQuizAttempt::getPercentage)
                    .min()
                    .orElse(0.0);
            stats.put("minScore", minScore);
        }
        
        return stats;
    }
} 