package com.elearning.elearning_backend.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.elearning.elearning_backend.DTO.CourseProgress;
import com.elearning.elearning_backend.DTO.MonthlyProgress;
import com.elearning.elearning_backend.DTO.SystemStatsResponse;
import com.elearning.elearning_backend.DTO.TeacherCourseStats;
import com.elearning.elearning_backend.DTO.UserAnalyticsResponse;
import com.elearning.elearning_backend.Enum.Role;
import com.elearning.elearning_backend.Repository.CourseRepository;
import com.elearning.elearning_backend.Repository.LessonProgressRepository;
import com.elearning.elearning_backend.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final CourseRepository courseRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final UserRepository userRepository;

    public UserAnalyticsResponse getUserAnalytics(String userId) {
        int courseCount = courseRepository.countByEnrolledUserIdsContaining(userId);

        int lessonsDone = lessonProgressRepository.countByUserIdAndCompletedTrue(userId);


        return new UserAnalyticsResponse(courseCount, lessonsDone);
    }
    public List<CourseProgress> getCourseProgress(String userId) {
        var courses = courseRepository.findByEnrolledUserIdsContaining(userId);


        return courses.stream().map(course -> {
            int completed = lessonProgressRepository.countByUserIdAndCourseIdAndCompletedTrue(userId, course.getId());

            return new CourseProgress(
                    course.getId(),
                    course.getTitle(),
                    completed,
                    course.getLessons().size()
            );
        }).collect(Collectors.toList());
    }
    public List<MonthlyProgress> getMonthlyProgress(String userId, int year) {
        List<MonthlyProgress> stats = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime end = start.plusMonths(1).minusNanos(1);

            int count = lessonProgressRepository.countByUserIdAndCompletedAtBetween(userId, start, end);

            stats.add(new MonthlyProgress(String.format("%d-%02d", year, month), count));
        }

        return stats;
    }
    public List<TeacherCourseStats> getTeacherCourseStats(String teacherId) {
        var courses = courseRepository.findByCreatedByUserId(teacherId);

        return courses.stream().map(course -> {
            List<String> studentIds = course.getEnrolledUserIds();
            int totalLessons = course.getLessons().size();

            double avgCompleted = 0.0;
            if (!studentIds.isEmpty()) {
                int totalCompleted = studentIds.stream()
                        .mapToInt(uid -> lessonProgressRepository.countByUserIdAndCourseIdAndCompletedTrue(uid, course.getId()))
                        .sum();
                avgCompleted = (double) totalCompleted / studentIds.size();
            }

            return new TeacherCourseStats(
                    course.getId(),
                    course.getTitle(),
                    studentIds.size(),
                    totalLessons,
                    avgCompleted
            );
        }).collect(Collectors.toList());
    }
    public SystemStatsResponse getSystemStats() {
        int students = (int) userRepository.countByRole(Role.STUDENT);
        int teachers = (int) userRepository.countByRole(Role.TEACHER);
        int courses = (int) courseRepository.count();
        int lessons = courseRepository.findAll().stream()
                .mapToInt(c -> c.getLessons().size())
                .sum();

        return new SystemStatsResponse(students, teachers, courses, lessons);
    }


}
