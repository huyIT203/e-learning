package com.elearning.elearning_backend.Service;

import com.elearning.elearning_backend.Model.Quiz;
import com.elearning.elearning_backend.Repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    // Tạo quiz mới
    public Quiz createQuiz(Quiz quiz) {
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());
        return quizRepository.save(quiz);
    }

    // Lấy tất cả quiz của một bài học
    public List<Quiz> getQuizzesByLessonId(String lessonId) {
        return quizRepository.findByLessonIdOrderByPosition(lessonId);
    }

    // Lấy quiz theo ID
    public Optional<Quiz> getQuizById(String quizId) {
        return quizRepository.findById(quizId);
    }

    // Cập nhật quiz
    public Quiz updateQuiz(String quizId, Quiz updatedQuiz) {
        Optional<Quiz> existingQuiz = quizRepository.findById(quizId);
        if (existingQuiz.isPresent()) {
            Quiz quiz = existingQuiz.get();
            quiz.setTitle(updatedQuiz.getTitle());
            quiz.setDescription(updatedQuiz.getDescription());
            quiz.setQuestions(updatedQuiz.getQuestions());
            quiz.setMaxScore(updatedQuiz.getMaxScore());
            quiz.setTimeLimit(updatedQuiz.getTimeLimit());
            quiz.setShowFeedback(updatedQuiz.isShowFeedback());
            quiz.setShowCorrectAnswer(updatedQuiz.isShowCorrectAnswer());
            quiz.setUpdatedAt(LocalDateTime.now());
            return quizRepository.save(quiz);
        }
        return null;
    }

    // Xóa quiz
    public boolean deleteQuiz(String quizId) {
        if (quizRepository.existsById(quizId)) {
            quizRepository.deleteById(quizId);
            return true;
        }
        return false;
    }

    // Thêm quiz vào vị trí cụ thể trong bài học
    public Quiz insertQuizAtPosition(String lessonId, Quiz quiz, int position) {
        // Dịch chuyển các quiz có position >= position lên 1
        List<Quiz> quizzesToMove = quizRepository.findByLessonIdAndPositionGreaterThan(lessonId, position - 1);
        for (Quiz q : quizzesToMove) {
            q.setPosition(q.getPosition() + 1);
            quizRepository.save(q);
        }

        // Đặt quiz mới vào vị trí
        quiz.setLessonId(lessonId);
        quiz.setPosition(position);
        return createQuiz(quiz);
    }

    // Kiểm tra câu trả lời
    public Map<String, Object> checkAnswer(String quizId, Map<String, Object> userAnswers) {
        Map<String, Object> result = new HashMap<>();
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        
        if (!quizOpt.isPresent()) {
            result.put("error", "Quiz not found");
            return result;
        }

        Quiz quiz = quizOpt.get();
        List<Quiz.Question> questions = quiz.getQuestions();
        
        int totalQuestions = questions.size();
        int correctAnswers = 0;
        Map<String, Object> questionResults = new HashMap<>();

        for (Quiz.Question question : questions) {
            String questionId = question.getId();
            Object userAnswer = userAnswers.get(questionId);
            boolean isCorrect = checkSingleAnswer(question, userAnswer);
            
            if (isCorrect) {
                correctAnswers++;
            }

            Map<String, Object> questionResult = new HashMap<>();
            questionResult.put("correct", isCorrect);
            questionResult.put("correctAnswer", question.getCorrectAnswer());
            questionResult.put("explanation", question.getExplanation());
            questionResult.put("userAnswer", userAnswer);
            
            questionResults.put(questionId, questionResult);
        }

        // Tính phần trăm
        double percentage = totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0;
        
        result.put("totalQuestions", totalQuestions);
        result.put("correctAnswers", correctAnswers);
        result.put("percentage", Math.round(percentage * 100.0) / 100.0);
        result.put("questionResults", questionResults);
        result.put("showFeedback", quiz.isShowFeedback());
        result.put("showCorrectAnswer", quiz.isShowCorrectAnswer());

        return result;
    }

    // Kiểm tra một câu trả lời
    private boolean checkSingleAnswer(Quiz.Question question, Object userAnswer) {
        if (userAnswer == null) return false;

        switch (question.getType()) {
            case MULTIPLE_CHOICE:
            case TRUE_FALSE:
                return question.getCorrectAnswer().equals(userAnswer.toString());
            
            case FILL_BLANK:
                String[] correctAnswers = question.getCorrectAnswer().split(",");
                String userAnswerStr = userAnswer.toString().toLowerCase().trim();
                for (String correct : correctAnswers) {
                    if (correct.toLowerCase().trim().equals(userAnswerStr)) {
                        return true;
                    }
                }
                return false;
            
            case MATCHING:
            case ORDERING:
                // Cho các loại này, userAnswer sẽ là một array hoặc object
                return question.getCorrectAnswer().equals(userAnswer.toString());
            
            case ESSAY:
                // Essay thường cần chấm thủ công, trả về true tạm thời
                return true;
            
            default:
                return false;
        }
    }

    // Lấy vị trí tiếp theo trong bài học
    public int getNextPosition(String lessonId) {
        long count = quizRepository.countByLessonId(lessonId);
        return (int) count + 1;
    }

    // Cập nhật vị trí quiz
    public Quiz updateQuizPosition(String quizId, int newPosition) {
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (quizOpt.isPresent()) {
            Quiz quiz = quizOpt.get();
            quiz.setPosition(newPosition);
            quiz.setUpdatedAt(LocalDateTime.now());
            return quizRepository.save(quiz);
        }
        return null;
    }

    // Tạo quiz mẫu cho demo
    public Quiz createSampleQuiz(String lessonId, Quiz.QuizType type) {
        Quiz quiz = new Quiz();
        quiz.setLessonId(lessonId);
        quiz.setType(type);
        quiz.setShowFeedback(true);
        quiz.setShowCorrectAnswer(true);

        switch (type) {
            case SINGLE_QUESTION:
                quiz.setTitle("Câu hỏi kiểm tra");
                quiz.setDescription("Một câu hỏi để kiểm tra hiểu biết");
                break;
            case MULTIPLE_QUESTIONS:
                quiz.setTitle("Bài tập trắc nghiệm");
                quiz.setDescription("Một bài tập với nhiều câu hỏi");
                break;
            case FINAL_TEST:
                quiz.setTitle("Bài kiểm tra cuối bài");
                quiz.setDescription("Bài kiểm tra tổng hợp kiến thức");
                quiz.setTimeLimit(30); // 30 phút
                break;
        }

        return createQuiz(quiz);
    }
} 