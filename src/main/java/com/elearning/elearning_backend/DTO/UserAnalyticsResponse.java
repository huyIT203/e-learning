package com.elearning.elearning_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAnalyticsResponse {
    private int totalCourses;
    private int totalLessonsCompleted;
    private int totalExamsSubmitted;
    private double averageScore;
}
