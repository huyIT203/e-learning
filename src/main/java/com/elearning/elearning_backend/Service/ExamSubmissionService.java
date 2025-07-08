package com.elearning.elearning_backend.Service;

import com.elearning.elearning_backend.DTO.AnswerRequest;
import com.elearning.elearning_backend.DTO.ExamSubmissionRequest;
import com.elearning.elearning_backend.Model.ExamSubmission;
import com.elearning.elearning_backend.Model.Question;
import com.elearning.elearning_backend.Model.StudentAnswer;

import com.elearning.elearning_backend.Repository.ExamSubmissionRepository;
import com.elearning.elearning_backend.Repository.QuestionRepository;
import com.elearning.elearning_backend.Repository.StudentAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamSubmissionService {
    private final ExamSubmissionRepository submissionRepo;
    private final QuestionRepository questionRepo;
    private final StudentAnswerRepository studentAnswerRepo;

    public double submitExam(String examId, String studentId, ExamSubmissionRequest request) {
        int total = 0, correct = 0;

        ExamSubmission submission = ExamSubmission.builder()
                .examId(examId)
                .studentId(studentId)
                .submittedAt(LocalDateTime.now())
                .build();

        submission = submissionRepo.save(submission);

        for (AnswerRequest answer : request.getAnswers()) {
            Question question = questionRepo.findById(answer.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            boolean isCorrect = question.getCorrectOptionIndex() == answer.getSelectedOptionIndex();

            StudentAnswer studentAnswer = StudentAnswer.builder()
                    .submissionId(submission.getId())
                    .questionId(answer.getQuestionId())
                    .selectedOptionIndex(answer.getSelectedOptionIndex())
                    .isCorrect(isCorrect)
                    .build();

            studentAnswerRepo.save(studentAnswer);

            total++;
            if (isCorrect) correct++;
        }

        double score = total == 0 ? 0 : (correct * 100.0 / total);
        submission.setScore(score);
        submissionRepo.save(submission);

        return score;
    }
    public ExamSubmission getSubmission(String examId, String studentId) {
        return submissionRepo.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new RuntimeException("No submission found"));
    }

    public List<ExamSubmission> getAllSubmissions(String examId) {
        return submissionRepo.findByExamId(examId);
    }

    public List<ExamSubmission> getAllSubmissionsAllExams() {
        return submissionRepo.findAll();
    }
}
