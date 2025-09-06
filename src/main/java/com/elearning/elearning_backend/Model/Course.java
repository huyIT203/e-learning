package com.elearning.elearning_backend.Model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.elearning.elearning_backend.Enum.CourseLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    private String id;
    private String title;
    private String description;
    private String image;
    private CourseLevel level;
    private String instructorName;
    private String createdByUserId;
    private String teacherEmail;
    private Integer duration;
    private String status;
    private String prerequisites;
    private String objectives;
    private List<String> enrolledUserIds;
    @Field("teacherId")
    private String teacherId;
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();
    
    // Category fields - changed from enum to String references
    private String jobRole; // References Category name for ROLE type
    private String skillCategory; // References Category name for SKILL type
}
