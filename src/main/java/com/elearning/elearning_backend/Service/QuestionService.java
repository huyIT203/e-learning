package com.elearning.elearning_backend.Service;

import com.elearning.elearning_backend.Model.Question;
import com.elearning.elearning_backend.Repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    public Question addQuestion(String examId, Question question) {
        question.setExamId(examId);
        return questionRepository.save(question);
    }

    public List<Question> getQuestionsByExamId(String examId) {
        return questionRepository.findByExamId(examId);
    }
}
