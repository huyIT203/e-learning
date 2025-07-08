package com.elearning.elearning_backend.Controller;

import com.elearning.elearning_backend.Model.Quiz;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Model.UserQuizAttempt;
import com.elearning.elearning_backend.Service.QuizService;
import com.elearning.elearning_backend.Service.UserQuizAttemptService;
import com.elearning.elearning_backend.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserQuizAttemptService userQuizAttemptService;

    @Autowired
    private UserService userService;

    // Tạo quiz mới
    @PostMapping("/lessons/{lessonId}")
    public ResponseEntity<?> createQuiz(@PathVariable String lessonId, @RequestBody Quiz quiz) {
        try {
            quiz.setLessonId(lessonId);
            Quiz createdQuiz = quizService.createQuiz(quiz);
            return ResponseEntity.ok(createdQuiz);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating quiz: " + e.getMessage());
        }
    }

    // Thêm quiz vào vị trí cụ thể
    @PostMapping("/lessons/{lessonId}/position/{position}")
    public ResponseEntity<?> insertQuizAtPosition(
            @PathVariable String lessonId, 
            @PathVariable int position,
            @RequestBody Quiz quiz) {
        try {
            Quiz createdQuiz = quizService.insertQuizAtPosition(lessonId, quiz, position);
            return ResponseEntity.ok(createdQuiz);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inserting quiz: " + e.getMessage());
        }
    }

    // Lấy tất cả quiz của một bài học
    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<?> getQuizzesByLessonId(@PathVariable String lessonId) {
        try {
            List<Quiz> quizzes = quizService.getQuizzesByLessonId(lessonId);
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching quizzes: " + e.getMessage());
        }
    }

    // Lấy quiz theo ID
    @GetMapping("/{quizId}")
    public ResponseEntity<?> getQuizById(@PathVariable String quizId) {
        try {
            Optional<Quiz> quiz = quizService.getQuizById(quizId);
            if (quiz.isPresent()) {
                return ResponseEntity.ok(quiz.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching quiz: " + e.getMessage());
        }
    }

    // Cập nhật quiz
    @PutMapping("/{quizId}")
    public ResponseEntity<?> updateQuiz(@PathVariable String quizId, @RequestBody Quiz quiz) {
        try {
            Quiz updatedQuiz = quizService.updateQuiz(quizId, quiz);
            if (updatedQuiz != null) {
                return ResponseEntity.ok(updatedQuiz);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating quiz: " + e.getMessage());
        }
    }

    // Xóa quiz
    @DeleteMapping("/{quizId}")
    public ResponseEntity<?> deleteQuiz(@PathVariable String quizId) {
        try {
            boolean deleted = quizService.deleteQuiz(quizId);
            if (deleted) {
                return ResponseEntity.ok("Quiz deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting quiz: " + e.getMessage());
        }
    }

    // Kiểm tra câu trả lời
    @PostMapping("/{quizId}/check")
    public ResponseEntity<?> checkAnswer(
            @PathVariable String quizId, 
            @RequestBody Map<String, Object> userAnswers) {
        try {
            Map<String, Object> result = quizService.checkAnswer(quizId, userAnswers);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking answers: " + e.getMessage());
        }
    }

    // Cập nhật vị trí quiz
    @PutMapping("/{quizId}/position/{newPosition}")
    public ResponseEntity<?> updateQuizPosition(
            @PathVariable String quizId, 
            @PathVariable int newPosition) {
        try {
            Quiz updatedQuiz = quizService.updateQuizPosition(quizId, newPosition);
            if (updatedQuiz != null) {
                return ResponseEntity.ok(updatedQuiz);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating quiz position: " + e.getMessage());
        }
    }

    // Lấy vị trí tiếp theo
    @GetMapping("/lessons/{lessonId}/next-position")
    public ResponseEntity<?> getNextPosition(@PathVariable String lessonId) {
        try {
            int nextPosition = quizService.getNextPosition(lessonId);
            return ResponseEntity.ok(Map.of("nextPosition", nextPosition));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting next position: " + e.getMessage());
        }
    }

    // Tạo quiz mẫu
    @PostMapping("/lessons/{lessonId}/sample/{type}")
    public ResponseEntity<?> createSampleQuiz(
            @PathVariable String lessonId, 
            @PathVariable String type) {
        try {
            Quiz.QuizType quizType = Quiz.QuizType.valueOf(type.toUpperCase());
            Quiz sampleQuiz = quizService.createSampleQuiz(lessonId, quizType);
            return ResponseEntity.ok(sampleQuiz);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid quiz type: " + type);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating sample quiz: " + e.getMessage());
        }
    }

    // === USER QUIZ ATTEMPT ENDPOINTS ===

    // Bắt đầu làm quiz hoặc lấy attempt hiện tại
    @PostMapping("/{quizId}/start")
    public ResponseEntity<?> startQuiz(
            @PathVariable String quizId,
            @RequestParam String lessonId,
            Authentication authentication) {
        try {
            if (authentication == null) {
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
            UserQuizAttempt attempt = userQuizAttemptService.startOrGetAttempt(userId, quizId, lessonId);
            
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error starting quiz: " + e.getMessage());
        }
    }

    // Lưu câu trả lời tạm thời (auto-save)
    @PostMapping("/attempts/{attemptId}/save")
    public ResponseEntity<?> saveAnswer(
            @PathVariable String attemptId,
            @RequestBody Map<String, Object> answerData) {
        try {
            String questionId = (String) answerData.get("questionId");
            Object answer = answerData.get("answer");
            
            UserQuizAttempt attempt = userQuizAttemptService.saveAnswer(attemptId, questionId, answer);
            
            if (attempt != null) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Answer saved"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving answer: " + e.getMessage());
        }
    }

    // Nộp bài quiz
    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<?> submitQuiz(
            @PathVariable String attemptId,
            @RequestBody Map<String, Object> finalAnswers) {
        try {
            UserQuizAttempt attempt = userQuizAttemptService.submitQuiz(attemptId, finalAnswers);
            
            if (attempt != null) {
                return ResponseEntity.ok(attempt);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error submitting quiz: " + e.getMessage());
        }
    }

    // Lấy kết quả quiz của user
    @GetMapping("/{quizId}/result")
    public ResponseEntity<?> getQuizResult(
            @PathVariable String quizId,
            Authentication authentication) {
        try {
            if (authentication == null) {
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
                return ResponseEntity.ok(attempt.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching quiz result: " + e.getMessage());
        }
    }

    // Reset quiz (cho phép làm lại)
    @PostMapping("/{quizId}/reset")
    public ResponseEntity<?> resetQuiz(
            @PathVariable String quizId,
            @RequestParam String lessonId,
            Authentication authentication) {
        try {
            if (authentication == null) {
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
            
            return ResponseEntity.ok(newAttempt);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error resetting quiz: " + e.getMessage());
        }
    }

    // Lấy thống kê quiz (cho giảng viên)
    @GetMapping("/{quizId}/stats")
    public ResponseEntity<?> getQuizStats(@PathVariable String quizId) {
        try {
            Map<String, Object> stats = userQuizAttemptService.getQuizStats(quizId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching quiz stats: " + e.getMessage());
        }
    }

    // Lấy tất cả attempts của user cho lesson (cho theo dõi tiến độ)
    @GetMapping("/lessons/{lessonId}/attempts")
    public ResponseEntity<?> getUserAttemptsForLesson(
            @PathVariable String lessonId,
            Authentication authentication) {
        try {
            if (authentication == null) {
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
            List<UserQuizAttempt> attempts = userQuizAttemptService.getUserAttemptsForLesson(userId, lessonId);
            
            return ResponseEntity.ok(attempts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching user attempts: " + e.getMessage());
        }
    }
} 