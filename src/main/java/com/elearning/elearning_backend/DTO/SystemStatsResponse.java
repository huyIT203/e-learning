package com.elearning.elearning_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemStatsResponse {
    private int totalStudents;
    private int totalTeachers;
    private int totalCourses;
    private int totalLessons;
}
