package com.elearning.elearning_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseStats {
    private long totalCourses;
    private long totalTeachers;
    private long totalStudents;
    private long totalLessons;
    private long totalEnrollments;
} 