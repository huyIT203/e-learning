package com.elearning.elearning_backend.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.elearning.elearning_backend.Enum.CourseLevel;
import com.elearning.elearning_backend.Model.Category.CategoryType;
import com.elearning.elearning_backend.Model.Course;
import com.elearning.elearning_backend.Model.Lesson;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.UserRepository;
import com.elearning.elearning_backend.Service.CategoryService;
import com.elearning.elearning_backend.Service.CommentService;
import com.elearning.elearning_backend.Service.CourseService;
import com.elearning.elearning_backend.Service.LessonProgressService;
import com.elearning.elearning_backend.Service.LessonService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthPageController {
    private final CourseService courseService;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final LessonService lessonService;
    private final LessonProgressService lessonProgressService;
    private final CommentService commentService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        
        if (logout != null) {
            model.addAttribute("logout", "You have been logged out successfully");
        }
        
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPage() {
        return "forgot-password";
    }

    @GetMapping("/session-expired")
    public String sessionExpiredPage() {
        return "session-expired";
    }

    @GetMapping("/courses")
    public String allCoursesPage(
            @RequestParam(required = false) String jobRole,
            @RequestParam(required = false) String skillCategory,
            @RequestParam(required = false) String levels,
            Model model) {
        // Check if user is authenticated and add user info to model
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            currentUser = userRepository.findByEmail(email).orElse(null);
        }
        
        // Add user to model (will be null if not authenticated)
        model.addAttribute("currentUser", currentUser);
        
        // Parse levels parameter
        List<CourseLevel> selectedLevels = new ArrayList<>();
        if (levels != null && !levels.isEmpty()) {
            String[] levelArray = levels.split(",");
            for (String level : levelArray) {
                try {
                    selectedLevels.add(CourseLevel.valueOf(level.trim()));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid level values
                }
            }
        }
        
        // Get courses with filtering by category names
        List<Course> courses = courseService.getPublishedCourses();
        
        // Filter by jobRole if specified
        if (jobRole != null && !jobRole.isEmpty()) {
            courses = courses.stream()
                    .filter(course -> jobRole.equals(course.getJobRole()))
                    .collect(Collectors.toList());
        }
        
        // Filter by skillCategory if specified
        if (skillCategory != null && !skillCategory.isEmpty()) {
            courses = courses.stream()
                    .filter(course -> skillCategory.equals(course.getSkillCategory()))
                    .collect(Collectors.toList());
        }
        
        // Filter by levels if specified
        if (!selectedLevels.isEmpty()) {
            courses = courses.stream()
                    .filter(course -> selectedLevels.contains(course.getLevel()))
                    .collect(Collectors.toList());
        }

        // Create course data with rating information
        List<Map<String, Object>> coursesWithRating = new ArrayList<>();
        for (Course course : courses) {
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("course", course);
            
            // Get rating stats for this course
            Map<String, Object> ratingStats = commentService.getCourseRatingStats(course.getId());
            double averageRating = (Double) ratingStats.getOrDefault("averageRating", 0.0);
            long totalComments = (Long) ratingStats.getOrDefault("totalComments", 0L);
            
            courseData.put("averageRating", averageRating);
            courseData.put("totalReviews", totalComments);
            
            coursesWithRating.add(courseData);
        }
        
        model.addAttribute("courses", courses);
        model.addAttribute("coursesWithRating", coursesWithRating);
        model.addAttribute("selectedJobRole", jobRole);
        model.addAttribute("selectedSkillCategory", skillCategory);
        model.addAttribute("selectedLevels", selectedLevels);
        
        // Add categories from database instead of enums
        model.addAttribute("jobRoles", categoryService.getCategoriesByType(CategoryType.ROLE));
        model.addAttribute("skillCategories", categoryService.getCategoriesByType(CategoryType.SKILL));
        model.addAttribute("courseLevels", CourseLevel.values());
        
        return "courses";
    }

    @GetMapping("/")
    public String home(Model model) {
        // Check if user is authenticated and add user info to model
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            currentUser = userRepository.findByEmail(email).orElse(null);
        }
        
        // Add user to model (will be null if not authenticated)
        model.addAttribute("currentUser", currentUser);

        List<Course> featuredCourses = courseService.getPublishedCourses(); // Only show published courses
        if (featuredCourses.size() > 6) {
            featuredCourses = featuredCourses.subList(0, 6);
        }

        // Create featured courses data with rating information
        List<Map<String, Object>> featuredCoursesWithRating = new ArrayList<>();
        for (Course course : featuredCourses) {
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("course", course);
            
            // Get rating stats for this course
            Map<String, Object> ratingStats = commentService.getCourseRatingStats(course.getId());
            double averageRating = (Double) ratingStats.getOrDefault("averageRating", 0.0);
            long totalComments = (Long) ratingStats.getOrDefault("totalComments", 0L);
            
            courseData.put("averageRating", averageRating);
            courseData.put("totalReviews", totalComments);
            
            featuredCoursesWithRating.add(courseData);
        }

        model.addAttribute("featuredCourses", featuredCourses);
        model.addAttribute("featuredCoursesWithRating", featuredCoursesWithRating);
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            System.out.println("⚠️ Người dùng chưa đăng nhập.");
            return "redirect:/login";
        }

        String email = auth.getName();
        System.out.println("✅ Truy cập dashboard với email: " + email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            System.out.println("❌ Không tìm thấy người dùng trong DB.");
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        switch (user.getRole()) {
            case ADMIN:
                return "redirect:/admin/dashboard";
            case TEACHER:
                return "redirect:/teacher/dashboard";
            case STUDENT:
                // Student dashboard is their courses page
                return "redirect:/student/dashboard";
            default:
                return "redirect:/error?code=403";
        }
    }

    @GetMapping("/course/{courseId}/lessons")
    public String courseLessons(@PathVariable String courseId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        
        if (currentUser == null) {
            return "redirect:/login";
        }

        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            return "redirect:/courses";
        }

        // Role-based access control
        if (currentUser.getRole() == com.elearning.elearning_backend.Enum.Role.STUDENT) {
            // For students: check if they are enrolled in the course
            if (course.getEnrolledUserIds() == null || !course.getEnrolledUserIds().contains(currentUser.getId())) {
                // Student is not enrolled, redirect to course detail page
                return "redirect:/course/" + courseId;
            }
        } else if (currentUser.getRole() == com.elearning.elearning_backend.Enum.Role.TEACHER) {
            // For teachers: check if they own the course
            if (!course.getCreatedByUserId().equals(currentUser.getId())) {
                // Teacher doesn't own this course, redirect to their courses page
                return "redirect:/teacher/courses";
            }
        } else if (currentUser.getRole() == com.elearning.elearning_backend.Enum.Role.ADMIN) {
            // Admin can access all courses
        } else {
            // Other roles are not allowed
            return "redirect:/error?code=403";
        }

        // Get actual lessons for the course
        List<Lesson> lessons = lessonService.getLessonsByCourseId(courseId);
        
        // Add progress information for the current user (mainly for students)
        boolean courseStarted = lessonProgressService.hasCourseStarted(currentUser.getId(), courseId);
        int completedLessons = lessonProgressService.getCompletedLessonsCount(currentUser.getId(), courseId);
        
        // Get completed lesson IDs
        List<String> completedLessonIds = lessonProgressService.getCourseProgress(currentUser.getId(), courseId)
            .stream()
            .filter(progress -> progress.isCompleted())
            .map(progress -> progress.getLessonId())
            .collect(Collectors.toList());

        // Add all required variables for the template
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("courseStarted", courseStarted);
        model.addAttribute("completedLessons", completedLessons);
        model.addAttribute("totalLessons", lessons.size());
        model.addAttribute("completedLessonIds", completedLessonIds);
        
        return "shared/lesson_list";
    }

    @GetMapping("/welcome")
    public String welcome(Model model) {
        // Check if user is authenticated and add user info to model
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            currentUser = userRepository.findByEmail(email).orElse(null);
        }
        
        // Add user to model (will be null if not authenticated)
        model.addAttribute("currentUser", currentUser);

        List<Course> featuredCourses = courseService.getPublishedCourses(); // Only show published courses
        if (featuredCourses.size() > 6) {
            featuredCourses = featuredCourses.subList(0, 6);
        }

        // Create featured courses data with rating information
        List<Map<String, Object>> featuredCoursesWithRating = new ArrayList<>();
        for (Course course : featuredCourses) {
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("course", course);
            
            // Get rating stats for this course
            Map<String, Object> ratingStats = commentService.getCourseRatingStats(course.getId());
            double averageRating = (Double) ratingStats.getOrDefault("averageRating", 0.0);
            long totalComments = (Long) ratingStats.getOrDefault("totalComments", 0L);
            
            courseData.put("averageRating", averageRating);
            courseData.put("totalReviews", totalComments);
            
            featuredCoursesWithRating.add(courseData);
        }

        model.addAttribute("featuredCourses", featuredCourses);
        model.addAttribute("featuredCoursesWithRating", featuredCoursesWithRating);
        
        // Add welcome message
        model.addAttribute("welcomeMessage", "Welcome to Esoclusty! Start exploring our courses.");
        
        return "index";
    }
}
