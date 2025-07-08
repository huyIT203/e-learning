package com.elearning.elearning_backend.Controller;

import com.elearning.elearning_backend.DTO.*;
import com.elearning.elearning_backend.Service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserAnalyticsResponse> getUserStats(@PathVariable String userId) {
        return ResponseEntity.ok(analyticsService.getUserAnalytics(userId));
    }

    @GetMapping("/user/{userId}/courses")
    public ResponseEntity<List<CourseProgress>> getPerCourseStats(@PathVariable String userId) {
        return ResponseEntity.ok(analyticsService.getCourseProgress(userId));
    }

    @GetMapping("/user/{userId}/monthly")
    public ResponseEntity<List<MonthlyProgress>> getMonthlyStats(
            @PathVariable String userId,
            @RequestParam int year
    ) {
        return ResponseEntity.ok(analyticsService.getMonthlyProgress(userId, year));
    }
    @GetMapping("/teacher/{teacherId}/courses")
    public ResponseEntity<List<TeacherCourseStats>> getTeacherStats(@PathVariable String teacherId) {
        return ResponseEntity.ok(analyticsService.getTeacherCourseStats(teacherId));
    }
    @GetMapping("/admin/overview")
    public ResponseEntity<SystemStatsResponse> getSystemStats() {
        return ResponseEntity.ok(analyticsService.getSystemStats());
    }

}
