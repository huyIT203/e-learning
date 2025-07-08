package com.elearning.elearning_backend.Service;

import com.elearning.elearning_backend.Model.Exam;
import com.elearning.elearning_backend.Repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;
    public Exam createExam(String courseId, Exam exam) {
        exam.setCourseId(courseId);
        exam.setCreatedAt(LocalDateTime.now());
        return examRepository.save(exam);
    }

    public List<Exam> getExamsByCourse(String courseId) {
        return examRepository.findByCourseId(courseId);
    }

    public Optional<Exam> getExamById(String examId) {
        return examRepository.findById(examId);
    }
}
