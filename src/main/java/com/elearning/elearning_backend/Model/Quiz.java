package com.elearning.elearning_backend.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "quizzes")
public class Quiz {
    @Id
    private String id;
    private String lessonId;
    private String title;
    private String description;
    private QuizType type;
    private int position; // Vị trí trong bài học
    private List<Question> questions;
    private int maxScore;
    private int timeLimit; // Thời gian làm bài (phút)
    private boolean showFeedback;
    private boolean showCorrectAnswer;
    private boolean embedded; // Có phải quiz nhúng trong nội dung không
    private boolean showResult; // Hiển thị kết quả ngay sau khi làm
    private boolean allowRetry; // Cho phép làm lại
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Quiz() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Quiz(String lessonId, String title, QuizType type) {
        this();
        this.lessonId = lessonId;
        this.title = title;
        this.type = type;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public QuizType getType() {
        return type;
    }

    public void setType(QuizType type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isShowFeedback() {
        return showFeedback;
    }

    public void setShowFeedback(boolean showFeedback) {
        this.showFeedback = showFeedback;
    }

    public boolean isShowCorrectAnswer() {
        return showCorrectAnswer;
    }

    public void setShowCorrectAnswer(boolean showCorrectAnswer) {
        this.showCorrectAnswer = showCorrectAnswer;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    public boolean isShowResult() {
        return showResult;
    }

    public void setShowResult(boolean showResult) {
        this.showResult = showResult;
    }

    public boolean isAllowRetry() {
        return allowRetry;
    }

    public void setAllowRetry(boolean allowRetry) {
        this.allowRetry = allowRetry;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Inner Classes
    public enum QuizType {
        SINGLE_QUESTION,     // Quiz 1 câu
        MULTIPLE_QUESTIONS,  // Quiz nhiều câu
        FINAL_TEST,          // Bài kiểm tra cuối bài
        MULTIPLE_CHOICE,     // Trắc nghiệm nhúng
        TRUE_FALSE,          // Đúng/Sai nhúng
        FILL_BLANK          // Điền vào chỗ trống nhúng
    }

    public static class Question {
        private String id;
        private String questionText;
        private QuestionType type;
        private List<Option> options;
        private String correctAnswer;
        private String explanation;
        private int points;
        private Map<String, Object> additionalData; // Cho các loại câu hỏi đặc biệt

        // Constructors
        public Question() {}

        public Question(String questionText, QuestionType type) {
            this.questionText = questionText;
            this.type = type;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public QuestionType getType() {
            return type;
        }

        public void setType(QuestionType type) {
            this.type = type;
        }

        public List<Option> getOptions() {
            return options;
        }

        public void setOptions(List<Option> options) {
            this.options = options;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public Map<String, Object> getAdditionalData() {
            return additionalData;
        }

        public void setAdditionalData(Map<String, Object> additionalData) {
            this.additionalData = additionalData;
        }

        public enum QuestionType {
            MULTIPLE_CHOICE,    // Trắc nghiệm
            TRUE_FALSE,         // Đúng/Sai
            ESSAY,              // Tự luận
            MATCHING,           // Nối câu
            FILL_BLANK,         // Điền vào chỗ trống
            ORDERING            // Sắp xếp
        }
    }

    public static class Option {
        private String id;
        private String text;
        private boolean isCorrect;

        // Constructors
        public Option() {}

        public Option(String text, boolean isCorrect) {
            this.text = text;
            this.isCorrect = isCorrect;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public boolean isCorrect() {
            return isCorrect;
        }

        public void setCorrect(boolean correct) {
            isCorrect = correct;
        }
    }
} 