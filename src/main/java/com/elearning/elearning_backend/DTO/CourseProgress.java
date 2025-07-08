package com.elearning.elearning_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseProgress {
    private String courseId;
    private String courseTitle;
    private int lessonsCompleted;
    private int totalLessons;
}
