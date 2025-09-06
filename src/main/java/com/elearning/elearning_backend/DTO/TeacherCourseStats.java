package com.elearning.elearning_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class    TeacherCourseStats {
    private String courseId;
    private String courseTitle;
    private int totalCourses;
    private int totalStudents;
    private int totalLessons;
    private int pendingCourses;
    private double avgLessonsCompletedPerStudent;

    public TeacherCourseStats(String id, String title, int size, int totalLessons, double avgCompleted) {
    }
}
