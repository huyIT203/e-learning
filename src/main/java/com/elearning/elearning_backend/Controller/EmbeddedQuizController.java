package com.elearning.elearning_backend.Controller;

import com.elearning.elearning_backend.Model.Quiz;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Model.UserQuizAttempt;
import com.elearning.elearning_backend.DTO.EmbeddedQuizRequest;
import com.elearning.elearning_backend.Service.QuizService;
import com.elearning.elearning_backend.Service.UserQuizAttemptService;
import com.elearning.elearning_backend.Service.UserService;
import com.elearning.elearning_backend.Service.EmbeddedQuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/embedded-quiz")
@CrossOrigin(origins = "*")
public class EmbeddedQuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserQuizAttemptService userQuizAttemptService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmbeddedQuizService embeddedQuizService;

    // Tạo quiz nhúng nhanh với template
    @PostMapping("/create-quick")
    public ResponseEntity<?> createQuickEmbeddedQuiz(
            @RequestBody Map<String, Object> requestData) {
        try {
            String lessonId = (String) requestData.get("lessonId");
            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            String type = (String) requestData.get("type"); // "MULTIPLE_CHOICE", "TRUE_FALSE", "FILL_BLANK"
            
            // Convert string type to enum
            Quiz.QuizType quizType;
            switch (type.toUpperCase()) {
                case "MULTIPLE_CHOICE":
                    quizType = Quiz.QuizType.MULTIPLE_CHOICE;
                    break;
                case "TRUE_FALSE":
                    quizType = Quiz.QuizType.TRUE_FALSE;
                    break;
                case "FILL_BLANK":
                    quizType = Quiz.QuizType.FILL_BLANK;
                    break;
                default:
                    quizType = Quiz.QuizType.MULTIPLE_CHOICE;
            }
            
            Quiz createdQuiz = embeddedQuizService.createEmbeddedQuizWithSample(lessonId, quizType, title, description);
            
            return ResponseEntity.ok(createdQuiz);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating embedded quiz: " + e.getMessage());
        }
    }

    // Tạo quiz nhúng với câu hỏi tùy chỉnh
    @PostMapping("/create-custom")
    public ResponseEntity<?> createCustomEmbeddedQuiz(
            @RequestBody EmbeddedQuizRequest request) {
        try {
            Quiz createdQuiz = embeddedQuizService.createCustomEmbeddedQuiz(request);
            return ResponseEntity.ok(createdQuiz);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating custom embedded quiz: " + e.getMessage());
        }
    }

    // Lấy quiz nhúng với thông tin attempt của user
    @GetMapping("/{quizId}")
    public ResponseEntity<?> getEmbeddedQuiz(
            @PathVariable String quizId,
            Authentication authentication) {
        try {
            Optional<Quiz> quizOpt = quizService.getQuizById(quizId);
            if (!quizOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Quiz quiz = quizOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("quiz", quiz);
            
            // Nếu user đã đăng nhập, lấy thông tin attempt
            if (authentication != null && !authentication.getName().equals("anonymousUser")) {
                String userEmail = authentication.getName();
                User user = userService.findByEmail(userEmail);
                
                if (user != null) {
                    String userId = user.getId();
                    Optional<UserQuizAttempt> attempt = userQuizAttemptService.getUserAttemptForQuiz(userId, quizId);
                    
                    response.put("userAttempt", attempt.orElse(null));
                    response.put("hasCompleted", userQuizAttemptService.hasUserCompletedQuiz(userId, quizId));
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching embedded quiz: " + e.getMessage());
        }
    }

    // Bắt đầu làm quiz nhúng
    @PostMapping("/{quizId}/start")
    public ResponseEntity<?> startEmbeddedQuiz(
            @PathVariable String quizId,
            @RequestParam String lessonId,
            Authentication authentication) {
        try {
            if (authentication == null || authentication.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User must be logged in to start quiz");
            }

            String userEmail = authentication.getName();
            User user = userService.findByEmail(userEmail);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not found");
            }

            String userId = user.getId();
            
            // Kiểm tra xem user đã hoàn thành quiz chưa
            if (userQuizAttemptService.hasUserCompletedQuiz(userId, quizId)) {
                Optional<UserQuizAttempt> existingAttempt = userQuizAttemptService.getUserAttemptForQuiz(userId, quizId);
                return ResponseEntity.ok(Map.of(
                    "attempt", existingAttempt.get(),
                    "message", "Quiz already completed",
                    "canRetry", true
                ));
            }
            
            UserQuizAttempt attempt = userQuizAttemptService.startOrGetAttempt(userId, quizId, lessonId);
            
            return ResponseEntity.ok(Map.of(
                "attempt", attempt,
                "message", "Quiz started successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error starting embedded quiz: " + e.getMessage());
        }
    }

    // Lưu câu trả lời (auto-save cho quiz nhúng)
    @PostMapping("/attempts/{attemptId}/save")
    public ResponseEntity<?> saveEmbeddedAnswer(
            @PathVariable String attemptId,
            @RequestBody Map<String, Object> answerData) {
        try {
            String questionId = (String) answerData.get("questionId");
            Object answer = answerData.get("answer");
            
            UserQuizAttempt attempt = userQuizAttemptService.saveAnswer(attemptId, questionId, answer);
            
            if (attempt != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "Answer saved automatically",
                    "timestamp", attempt.getUpdatedAt()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving embedded answer: " + e.getMessage());
        }
    }

    // Nộp bài quiz nhúng
    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<?> submitEmbeddedQuiz(
            @PathVariable String attemptId,
            @RequestBody Map<String, Object> finalAnswers) {
        try {
            UserQuizAttempt attempt = userQuizAttemptService.submitQuiz(attemptId, finalAnswers);
            
            if (attempt != null) {
                // Tính toán thêm thông tin cho quiz nhúng
                Map<String, Object> response = new HashMap<>();
                response.put("attempt", attempt);
                response.put("score", attempt.getScore());
                response.put("percentage", attempt.getPercentage());
                response.put("passed", attempt.getPercentage() >= 70); // Điểm đậu 70%
                response.put("totalQuestions", attempt.getTotalQuestions());
                response.put("correctAnswers", attempt.getCorrectAnswers());
                response.put("message", "Quiz submitted successfully");
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error submitting embedded quiz: " + e.getMessage());
        }
    }

    // Lấy kết quả chi tiết của quiz nhúng
    @GetMapping("/{quizId}/result")
    public ResponseEntity<?> getEmbeddedQuizResult(
            @PathVariable String quizId,
            Authentication authentication) {
        try {
            if (authentication == null || authentication.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User must be logged in");
            }

            String userEmail = authentication.getName();
            User user = userService.findByEmail(userEmail);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not found");
            }

            String userId = user.getId();
            Optional<UserQuizAttempt> attempt = userQuizAttemptService.getUserAttemptForQuiz(userId, quizId);
            
            if (attempt.isPresent()) {
                UserQuizAttempt userAttempt = attempt.get();
                Optional<Quiz> quizOpt = quizService.getQuizById(quizId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("attempt", userAttempt);
                response.put("quiz", quizOpt.orElse(null));
                response.put("canRetry", true); // Quiz nhúng luôn cho phép làm lại
                response.put("showCorrectAnswers", true);
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching embedded quiz result: " + e.getMessage());
        }
    }

    // Làm lại quiz nhúng
    @PostMapping("/{quizId}/retry")
    public ResponseEntity<?> retryEmbeddedQuiz(
            @PathVariable String quizId,
            @RequestParam String lessonId,
            Authentication authentication) {
        try {
            if (authentication == null || authentication.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User must be logged in");
            }

            String userEmail = authentication.getName();
            User user = userService.findByEmail(userEmail);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not found");
            }

            String userId = user.getId();
            UserQuizAttempt newAttempt = userQuizAttemptService.resetAttempt(userId, quizId, lessonId);
            
            return ResponseEntity.ok(Map.of(
                "attempt", newAttempt,
                "message", "Quiz reset successfully, you can start again"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrying embedded quiz: " + e.getMessage());
        }
    }

    // Lấy danh sách quiz nhúng trong bài học
    @GetMapping("/lessons/{lessonId}/embedded")
    public ResponseEntity<?> getEmbeddedQuizzesInLesson(
            @PathVariable String lessonId,
            Authentication authentication) {
        try {
            List<Quiz> allQuizzes = quizService.getQuizzesByLessonId(lessonId);
            
            // Lọc chỉ các quiz nhúng
            List<Quiz> embeddedQuizzes = allQuizzes.stream()
                    .filter(Quiz::isEmbedded)
                    .sorted((q1, q2) -> Integer.compare(q1.getPosition(), q2.getPosition()))
                    .collect(Collectors.toList());
            
            // Nếu user đã đăng nhập, thêm thông tin attempt
            if (authentication != null && !authentication.getName().equals("anonymousUser")) {
                String userEmail = authentication.getName();
                User user = userService.findByEmail(userEmail);
                
                if (user != null) {
                    String userId = user.getId();
                    List<UserQuizAttempt> attempts = userQuizAttemptService.getUserAttemptsForLesson(userId, lessonId);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("embeddedQuizzes", embeddedQuizzes);
                    response.put("userAttempts", attempts);
                    
                    return ResponseEntity.ok(response);
                }
            }
            
            return ResponseEntity.ok(Map.of("embeddedQuizzes", embeddedQuizzes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching embedded quizzes: " + e.getMessage());
        }
    }
} 