package com.elearning.elearning_backend.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.elearning.elearning_backend.Enum.Role;
import com.elearning.elearning_backend.Model.Course;
import com.elearning.elearning_backend.Model.Lesson;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.UserRepository;
import com.elearning.elearning_backend.Service.CourseService;
import com.elearning.elearning_backend.Service.LessonProgressService;
import com.elearning.elearning_backend.Service.LessonService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LessonDetailController {
    private final LessonService lessonService;
    private final CourseService courseService;
    private final LessonProgressService lessonProgressService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                String email = auth.getName();
                return userRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            // User not authenticated
        }
        return null;
    }

    @GetMapping("/lessons/{lessonId}")
    public String getLessonDetail(@PathVariable String lessonId, Model model) {
        Optional<Lesson> optionalLesson = lessonService.getLessonById(lessonId);
        if (optionalLesson.isEmpty()) return "error/404";

        Lesson lesson = optionalLesson.get();
        
        // Kiểm tra quyền truy cập
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            // Chưa đăng nhập - chuyển về trang khóa học
            return "redirect:/course/" + lesson.getCourseId() + "/lessons";
        }
        
        // Kiểm tra role và quyền truy cập
        Course course = courseService.getCourseById(lesson.getCourseId());
        if (course == null) {
            return "error/404";
        }
        
        // Logic kiểm tra quyền truy cập theo role
        if (currentUser.getRole() == Role.STUDENT) {
            // Với STUDENT: kiểm tra xem đã enroll và bắt đầu khóa học chưa
            if (!course.getEnrolledUserIds().contains(currentUser.getId())) {
                // Chưa enroll - chuyển về trang course detail
                return "redirect:/course/" + lesson.getCourseId();
            }
            
            boolean courseStarted = lessonProgressService.hasCourseStarted(currentUser.getId(), lesson.getCourseId());
            if (!courseStarted) {
                // Chưa bắt đầu khóa học - chuyển về trang khóa học
                return "redirect:/course/" + lesson.getCourseId() + "/lessons";
            }
        } else if (currentUser.getRole() == Role.TEACHER) {
            // Với TEACHER: chỉ cho phép truy cập khóa học của mình
            if (!course.getCreatedByUserId().equals(currentUser.getId())) {
                return "error/403";
            }
        } else if (currentUser.getRole() == Role.ADMIN) {
            // ADMIN có thể truy cập tất cả
        } else {
            // Role khác không được phép
            return "error/403";
        }
        
        model.addAttribute("lesson", lesson);

        // Mark lesson as accessed (chỉ cho STUDENT)
        if (currentUser.getRole() == Role.STUDENT) {
            lessonProgressService.markLessonCompleted(currentUser.getId(), lesson.getCourseId(), lessonId);
        }

        // Ưu tiên hiển thị contentHtml nếu có, nếu không thì parse từ file
        String bodyHtml = "";
        if (lesson.getContentHtml() != null && !lesson.getContentHtml().isEmpty()) {
            bodyHtml = lesson.getContentHtml();
        } else {
            try {
                if (lesson.getBodyPath() != null) {
                    if (lesson.getBodyPath().endsWith(".md")) {
                        bodyHtml = lessonService.parseMarkdown(lesson.getBodyPath());
                    } else if (lesson.getBodyPath().endsWith(".docx")) {
                        bodyHtml = lessonService.parseDocx(lesson.getBodyPath());
                    }
                }
            } catch (IOException e) {
                bodyHtml = "<p>Lỗi khi đọc nội dung bài học.</p>";
                e.printStackTrace();
            }
        }

        model.addAttribute("bodyHtml", bodyHtml);

        if (course != null) {
            model.addAttribute("courseTitle", course.getTitle());
            model.addAttribute("course", course);
            
            // Thêm danh sách tất cả bài học của khóa học để hiển thị trong sidebar
            List<Lesson> allLessons = lessonService.getLessonsByCourseId(lesson.getCourseId());
            model.addAttribute("allLessons", allLessons);
        }

        model.addAttribute("currentUser", currentUser);
        return "shared/lesson_detail";
    }

    @GetMapping("/courses/{courseId}/start")
    public String startCourse(@PathVariable String courseId, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Chỉ STUDENT mới có thể start course
        if (currentUser.getRole() != Role.STUDENT) {
            return "error/403";
        }
        
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            return "error/404";
        }
        
        // Kiểm tra xem đã enroll chưa
        if (!course.getEnrolledUserIds().contains(currentUser.getId())) {
            return "redirect:/course/" + courseId;
        }
        
        List<Lesson> lessons = lessonService.getLessonsByCourseId(courseId);
        if (lessons.isEmpty()) {
            return "redirect:/course/" + courseId + "/lessons";
        }
        
        // Kiểm tra xem đã bắt đầu khóa học chưa
        boolean courseStarted = lessonProgressService.hasCourseStarted(currentUser.getId(), courseId);
        
        if (courseStarted) {
            // Tiếp tục từ bài học cuối cùng đã truy cập
            String lastAccessedLessonId = lessonProgressService.getLastAccessedLessonId(currentUser.getId(), courseId);
            if (lastAccessedLessonId != null) {
                System.out.println("🟢 Continuing course - redirecting to last accessed lesson: " + lastAccessedLessonId);
                return "redirect:/lessons/" + lastAccessedLessonId;
            }
        } else {
            // Bắt đầu khóa học mới - tạo progress tracking cho bài học đầu tiên
            String firstLessonId = lessons.get(0).getId();
            System.out.println("🟢 Starting new course for user: " + currentUser.getEmail() + ", courseId: " + courseId);
            System.out.println("🟢 Creating initial progress for first lesson: " + firstLessonId);
            
            // Tạo progress entry cho bài học đầu tiên (chưa completed)
            lessonProgressService.markLessonAccessed(currentUser.getId(), courseId, firstLessonId);
        }
        
        // Bắt đầu từ bài học đầu tiên
        String firstLessonId = lessons.get(0).getId();
        System.out.println("🟢 Redirecting to first lesson: " + firstLessonId);
        return "redirect:/lessons/" + firstLessonId;
    }
}
