package com.elearning.elearning_backend.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.elearning.elearning_backend.DTO.CourseStats;
import com.elearning.elearning_backend.DTO.TeacherCourseStats;
import com.elearning.elearning_backend.Enum.CourseStatus;
import com.elearning.elearning_backend.Model.Course;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Service.CourseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;


    @PostMapping
    public ResponseEntity<Course> create(@RequestBody Course course) {
        return ResponseEntity.ok(courseService.createCourse(course));
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAll() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/published")
    public ResponseEntity<List<Course>> getPublishedCourses() {
        return ResponseEntity.ok(courseService.getPublishedCourses());
    }

    @GetMapping("/teacher")
    public ResponseEntity<List<Course>> getCoursesForTeacher(Authentication authentication) {
        String email = authentication.getName();
        System.out.println("[DEBUG] Teacher email: " + email);
        List<Course> courses = courseService.getCoursesByInstructorEmail(email);
        System.out.println("[DEBUG] Found " + courses.size() + " course(s) for " + email);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable String id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable String id, @RequestBody Course course) {
        return ResponseEntity.ok(courseService.updateCourse(id, course));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<User>> getTeachers() {
        return ResponseEntity.ok(courseService.getAllTeachers());
    }

    @GetMapping("/stats")
    public ResponseEntity<CourseStats> getStats() {
        return ResponseEntity.ok(courseService.getCourseStats());
    }
    
    // Category-based search endpoints
    @GetMapping("/by-job-role")
    public ResponseEntity<List<Course>> getCoursesByJobRole(@RequestParam String jobRole) {
        return ResponseEntity.ok(courseService.getCoursesByJobRole(jobRole));
    }
    
    @GetMapping("/by-skill")
    public ResponseEntity<List<Course>> getCoursesBySkill(@RequestParam String skillCategory) {
        return ResponseEntity.ok(courseService.getCoursesBySkillCategory(skillCategory));
    }
    
    @GetMapping("/by-category")
    public ResponseEntity<List<Course>> getCoursesByCategory(
            @RequestParam(required = false) String jobRole,
            @RequestParam(required = false) String skillCategory) {
        
        if (jobRole != null && skillCategory != null) {
            return ResponseEntity.ok(courseService.getCoursesByJobRoleAndSkill(jobRole, skillCategory));
        } else if (jobRole != null) {
            return ResponseEntity.ok(courseService.getCoursesByJobRole(jobRole));
        } else if (skillCategory != null) {
            return ResponseEntity.ok(courseService.getCoursesBySkillCategory(skillCategory));
        } else {
            return ResponseEntity.ok(courseService.getAllCourses());
        }
    }

    @GetMapping("/teacher/stats")
    public ResponseEntity<TeacherCourseStats> getStatsForTeacher(Authentication authentication) {
        try {
            System.out.println("🔍 Teacher stats request received");
            System.out.println("🔍 Authentication: " + authentication);
            
            if (authentication == null || authentication.getPrincipal() == null) {
                System.out.println("No authentication found");
                return ResponseEntity.status(401).build();
            }
            
            String teacherEmail = authentication.getName();
            System.out.println("👨‍🏫 Teacher Email: " + teacherEmail);
            
            // Get teacher user to find ID
            User teacher = courseService.getUserByEmail(teacherEmail);
            if (teacher == null) {
                System.out.println("Teacher not found with email: " + teacherEmail);
                return ResponseEntity.status(404).build();
            }
            
            String teacherId = teacher.getId();
            System.out.println("👨‍🏫 Teacher ID: " + teacherId);
            
            TeacherCourseStats stats = courseService.getStatsForTeacher(teacherId);
            
            System.out.println("📊 Returning stats: " + stats);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.out.println("Error getting teacher stats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> approve(@PathVariable String id) {
        return ResponseEntity.ok(courseService.updateStatus(id, CourseStatus.PUBLISHED));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> reject(@PathVariable String id) {
        return ResponseEntity.ok(courseService.updateStatus(id, CourseStatus.REJECTED));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Course>> getPendingCourses() {
        return ResponseEntity.ok(courseService.getCoursesByStatus(CourseStatus.PENDING));
    }
}
