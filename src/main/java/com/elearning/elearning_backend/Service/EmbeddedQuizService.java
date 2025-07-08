package com.elearning.elearning_backend.Service;

import com.elearning.elearning_backend.Model.Quiz;
import com.elearning.elearning_backend.DTO.EmbeddedQuizRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EmbeddedQuizService {

    @Autowired
    private QuizService quizService;

    public Quiz createEmbeddedQuizWithSample(String lessonId, Quiz.QuizType type, String title, String description) {
        Quiz quiz = new Quiz();
        quiz.setLessonId(lessonId);
        quiz.setTitle(title != null ? title : "Quiz không tiêu đề");
        quiz.setDescription(description != null ? description : "");
        quiz.setType(type);
        quiz.setPosition(quizService.getNextPosition(lessonId));
        quiz.setEmbedded(true);
        quiz.setShowResult(true);
        quiz.setAllowRetry(true);
        quiz.setTimeLimit(0);
        quiz.setShowFeedback(true);
        quiz.setShowCorrectAnswer(true);
        
        // Tạo câu hỏi mẫu dựa trên loại quiz
        List<Quiz.Question> sampleQuestions = createSampleQuestions(type);
        quiz.setQuestions(sampleQuestions);
        quiz.setMaxScore(calculateMaxScore(sampleQuestions));
        
        return quizService.createQuiz(quiz);
    }

    private List<Quiz.Question> createSampleQuestions(Quiz.QuizType type) {
        List<Quiz.Question> questions = new ArrayList<>();
        
        switch (type) {
            case MULTIPLE_CHOICE:
                questions.add(createMultipleChoiceQuestion());
                break;
            case TRUE_FALSE:
                questions.add(createTrueFalseQuestion());
                break;
            case FILL_BLANK:
                questions.add(createFillBlankQuestion());
                break;
            default:
                questions.add(createMultipleChoiceQuestion());
        }
        
        return questions;
    }

    private Quiz.Question createMultipleChoiceQuestion() {
        Quiz.Question question = new Quiz.Question();
        question.setId(UUID.randomUUID().toString());
        question.setQuestionText("Câu hỏi mẫu - Hãy chọn đáp án đúng:");
        question.setType(Quiz.Question.QuestionType.MULTIPLE_CHOICE);
        question.setPoints(10);
        question.setExplanation("Đây là giải thích cho câu hỏi mẫu");
        
        List<Quiz.Option> options = new ArrayList<>();
        options.add(new Quiz.Option("Đáp án A", false));
        options.add(new Quiz.Option("Đáp án B (Đúng)", true));
        options.add(new Quiz.Option("Đáp án C", false));
        options.add(new Quiz.Option("Đáp án D", false));
        
        // Set ID cho các option
        for (int i = 0; i < options.size(); i++) {
            options.get(i).setId(String.valueOf(i));
        }
        
        question.setOptions(options);
        question.setCorrectAnswer("1"); // Index của đáp án đúng
        
        return question;
    }

    private Quiz.Question createTrueFalseQuestion() {
        Quiz.Question question = new Quiz.Question();
        question.setId(UUID.randomUUID().toString());
        question.setQuestionText("Câu hỏi mẫu - Đúng hay Sai: Đây là một câu hỏi mẫu?");
        question.setType(Quiz.Question.QuestionType.TRUE_FALSE);
        question.setPoints(5);
        question.setExplanation("Đây là câu hỏi mẫu nên câu trả lời là Đúng");
        
        List<Quiz.Option> options = new ArrayList<>();
        options.add(new Quiz.Option("Đúng", true));
        options.add(new Quiz.Option("Sai", false));
        
        // Set ID cho các option
        options.get(0).setId("0");
        options.get(1).setId("1");
        
        question.setOptions(options);
        question.setCorrectAnswer("0"); // Index của đáp án "Đúng"
        
        return question;
    }

    private Quiz.Question createFillBlankQuestion() {
        Quiz.Question question = new Quiz.Question();
        question.setId(UUID.randomUUID().toString());
        question.setQuestionText("Câu hỏi mẫu - Điền vào chỗ trống: Java là một ngôn ngữ lập trình _____");
        question.setType(Quiz.Question.QuestionType.FILL_BLANK);
        question.setPoints(5);
        question.setCorrectAnswer("hướng đối tượng");
        question.setExplanation("Java là ngôn ngữ lập trình hướng đối tượng");
        
        return question;
    }

    private int calculateMaxScore(List<Quiz.Question> questions) {
        return questions.stream()
                .mapToInt(Quiz.Question::getPoints)
                .sum();
    }

    public Quiz createCustomEmbeddedQuiz(EmbeddedQuizRequest request) {
        Quiz quiz = new Quiz();
        quiz.setLessonId(request.getLessonId());
        quiz.setTitle(request.getTitle() != null ? request.getTitle() : "Quiz không tiêu đề");
        quiz.setDescription(request.getDescription() != null ? request.getDescription() : "");
        
        // Convert string type to enum
        Quiz.QuizType quizType;
        switch (request.getType().toUpperCase()) {
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
        
        quiz.setType(quizType);
        quiz.setPosition(request.getPosition() != null ? request.getPosition() : quizService.getNextPosition(request.getLessonId()));
        quiz.setEmbedded(request.getIsEmbedded() != null ? request.getIsEmbedded() : true);
        quiz.setShowResult(true);
        quiz.setAllowRetry(true);
        quiz.setTimeLimit(0);
        quiz.setShowFeedback(true);
        quiz.setShowCorrectAnswer(true);
        
        // Convert questions from request
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            List<Quiz.Question> questions = convertRequestQuestions(request.getQuestions());
            quiz.setQuestions(questions);
            quiz.setMaxScore(calculateMaxScore(questions));
        } else {
            // Create sample questions if none provided
            List<Quiz.Question> sampleQuestions = createSampleQuestions(quizType);
            quiz.setQuestions(sampleQuestions);
            quiz.setMaxScore(calculateMaxScore(sampleQuestions));
        }
        
        return quizService.createQuiz(quiz);
    }

    private List<Quiz.Question> convertRequestQuestions(List<EmbeddedQuizRequest.QuestionRequest> requestQuestions) {
        List<Quiz.Question> questions = new ArrayList<>();
        
        for (EmbeddedQuizRequest.QuestionRequest reqQuestion : requestQuestions) {
            Quiz.Question question = new Quiz.Question();
            question.setId(UUID.randomUUID().toString());
            question.setQuestionText(reqQuestion.getQuestionText());
            question.setPoints(reqQuestion.getPoints() != null ? reqQuestion.getPoints() : 10);
            question.setExplanation(reqQuestion.getExplanation());
            question.setCorrectAnswer(reqQuestion.getCorrectAnswer());
            
            // Convert question type
            Quiz.Question.QuestionType questionType;
            switch (reqQuestion.getType().toUpperCase()) {
                case "MULTIPLE_CHOICE":
                    questionType = Quiz.Question.QuestionType.MULTIPLE_CHOICE;
                    break;
                case "TRUE_FALSE":
                    questionType = Quiz.Question.QuestionType.TRUE_FALSE;
                    break;
                case "FILL_BLANK":
                    questionType = Quiz.Question.QuestionType.FILL_BLANK;
                    break;
                default:
                    questionType = Quiz.Question.QuestionType.MULTIPLE_CHOICE;
            }
            question.setType(questionType);
            
            // Convert options if provided
            if (reqQuestion.getOptions() != null && !reqQuestion.getOptions().isEmpty()) {
                List<Quiz.Option> options = new ArrayList<>();
                for (int i = 0; i < reqQuestion.getOptions().size(); i++) {
                    EmbeddedQuizRequest.OptionRequest reqOption = reqQuestion.getOptions().get(i);
                    Quiz.Option option = new Quiz.Option();
                    option.setId(String.valueOf(i));
                    option.setText(reqOption.getText());
                    option.setCorrect(reqOption.getIsCorrect() != null ? reqOption.getIsCorrect() : false);
                    options.add(option);
                }
                question.setOptions(options);
            }
            
            questions.add(question);
        }
        
        return questions;
    }
} 