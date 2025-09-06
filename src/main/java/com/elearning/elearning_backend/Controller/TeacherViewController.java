package com.elearning.elearning_backend.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.elearning.elearning_backend.Enum.Role;
import com.elearning.elearning_backend.Model.Comment;
import com.elearning.elearning_backend.Model.Course;
import com.elearning.elearning_backend.Model.Lesson;
import com.elearning.elearning_backend.Model.Notification;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.CourseRepository;
import com.elearning.elearning_backend.Repository.LessonRepository;
import com.elearning.elearning_backend.Repository.UserRepository;
import com.elearning.elearning_backend.Service.CategoryService;
import com.elearning.elearning_backend.Service.CommentService;
import com.elearning.elearning_backend.Service.CourseService;
import com.elearning.elearning_backend.Service.LessonProgressService;
import com.elearning.elearning_backend.Service.LessonService;
import com.elearning.elearning_backend.Service.NotificationService;
import com.elearning.elearning_backend.Service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherViewController {
    
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final LessonProgressService lessonProgressService;
    private final NotificationService notificationService;
    private final CommentService commentService;
    private final UserService userService;
    private final CategoryService categoryService;

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                String email = auth.getName();
                System.out.println("🟡 Getting user by email: " + email);
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    System.out.println("🟢 Found user: " + user.getEmail() + " with role: " + user.getRole());
                } else {
                    System.out.println("User not found in database for email: " + email);
                }
                return user;
            } else {
                System.out.println("Authentication is null, not authenticated, or anonymous user");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error getting current user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.TEACHER) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("role", currentUser.getRole());
        
        return "pages/dashboard_teacher";
    }

    @GetMapping("/courses")
    public String courses(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.TEACHER) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("role", currentUser.getRole());
        
        return "teacher/courses";
    }

    @GetMapping("/courses/{courseId}/lessons")
    public String courseLessons(@PathVariable String courseId, Model model) {
        User user = getCurrentUser();
        if (user == null) {
            System.out.println("User not authenticated for course lessons");
            return "redirect:/login";
        }
        
        // Allow both TEACHER and ADMIN to access
        if (user.getRole() != Role.TEACHER && user.getRole() != Role.ADMIN) {
            System.out.println("Access denied - user role: " + user.getRole());
            return "redirect:/login";
        }
        
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            System.out.println("Course not found: " + courseId);
            return "error/404";
        }
        
        // For TEACHER role, check if they own the course
        // For ADMIN role, allow access to all courses
        if (user.getRole() == Role.TEACHER && !course.getCreatedByUserId().equals(user.getId())) {
            System.out.println("Teacher doesn't own this course - Teacher: " + user.getId() + ", Course owner: " + course.getCreatedByUserId());
            return "error/403"; // Forbidden
        }
        
        List<Lesson> lessons = lessonService.getLessonsByCourseId(courseId);
        
        // Add progress information for the current user
        if (user != null) {
            boolean courseStarted = lessonProgressService.hasCourseStarted(user.getId(), courseId);
            String lastAccessedLessonId = lessonProgressService.getLastAccessedLessonId(user.getId(), courseId);
            int completedLessons = lessonProgressService.getCompletedLessonsCount(user.getId(), courseId);
            
            // Get completed lesson IDs
            List<String> completedLessonIds = lessonProgressService.getCourseProgress(user.getId(), courseId)
                .stream()
                .filter(progress -> progress.isCompleted())
                .map(progress -> progress.getLessonId())
                .toList();
            
            model.addAttribute("courseStarted", courseStarted);
            model.addAttribute("lastAccessedLessonId", lastAccessedLessonId);
            model.addAttribute("completedLessons", completedLessons);
            model.addAttribute("completedLessonIds", completedLessonIds);
            model.addAttribute("totalLessons", lessons.size());
        }
        
        model.addAttribute("currentUser", user);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        
        System.out.println("🟢 Successfully loaded course lessons page for user: " + user.getEmail());
        return "shared/lesson_list";
    }



    @GetMapping("/students")
    public String students(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.TEACHER) {
            return "redirect:/error";
        }
        model.addAttribute("user", user);
        return "teacher/students";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.TEACHER) {
            return "redirect:/error";
        }
        model.addAttribute("user", user);
        return "teacher/settings";
    }
    
    // Questions management
    @GetMapping("/questions")
    public String questions(Model model) {
        User user = getCurrentUser();
        if (user == null || (user.getRole() != Role.TEACHER && user.getRole() != Role.ADMIN)) {
            return "redirect:/error";
        }
        
        System.out.println("🟢 Loading questions page for user: " + user.getEmail());
        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole());
        return "teacher/questions";
    }
    
    // Exams management
    @GetMapping("/exams")
    public String exams(Model model) {
        User user = getCurrentUser();
        if (user == null || (user.getRole() != Role.TEACHER && user.getRole() != Role.ADMIN)) {
            return "redirect:/error";
        }
        
        System.out.println("🟢 Loading exams page for user: " + user.getEmail());
        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole());
        return "teacher/exams";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.TEACHER) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("role", currentUser.getRole());
        return "teacher/profile";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.TEACHER) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("role", currentUser.getRole());
        return "teacher/statistics";
    }

    @GetMapping("/messages")
    public String messages(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.TEACHER) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole());
        return "teacher/messages";
    }

    // API endpoints for profile management
    @PostMapping("/api/profile/personal")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePersonalInfo(
            @RequestParam("name") String name,
            @RequestParam("dob") String dob,
            @RequestParam("gender") String gender,
            @RequestParam("phone") String phone,
            @RequestParam("bio") String bio) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.TEACHER) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.badRequest().body(response);
            }

            // Update user information
            currentUser.setName(name);
            currentUser.setDob(dob);
            currentUser.setGender(gender);
            currentUser.setPhone(phone);
            currentUser.setBio(bio);

            userRepository.save(currentUser);

            response.put("success", true);
            response.put("message", "Cập nhật thông tin cá nhân thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/profile/contact")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateContactInfo(
            @RequestParam("email") String email,
            @RequestParam("facebookUrl") String facebookUrl,
            @RequestParam("githubUrl") String githubUrl) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.TEACHER) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.badRequest().body(response);
            }

            // Update contact information
            currentUser.setFacebookUrl(facebookUrl);
            currentUser.setGithubUrl(githubUrl);

            userRepository.save(currentUser);

            response.put("success", true);
            response.put("message", "Cập nhật thông tin liên hệ thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/profile/password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.TEACHER) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate current password
            if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                response.put("success", false);
                response.put("message", "Mật khẩu hiện tại không đúng");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate new password confirmation
            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "Mật khẩu xác nhận không khớp");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate password strength
            if (newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "Mật khẩu phải có ít nhất 6 ký tự");
                return ResponseEntity.badRequest().body(response);
            }

            // Update password
            currentUser.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(currentUser);

            response.put("success", true);
            response.put("message", "Đổi mật khẩu thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/profile/avatar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.TEACHER) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn file");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "Chỉ chấp nhận file hình ảnh");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "Kích thước file không được vượt quá 5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // Create upload directory if it doesn't exist
            String uploadDir = "src/main/resources/static/uploads/avatars/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String newFilename = "teacher_" + currentUser.getId() + "_" + UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update user avatar URL
            String avatarUrl = "/uploads/avatars/" + newFilename;
            currentUser.setAvatarUrl(avatarUrl);
            userRepository.save(currentUser);

            response.put("success", true);
            response.put("message", "Cập nhật avatar thành công");
            response.put("avatarUrl", avatarUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lưu file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTeacherStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> userOptional = userRepository.findByEmail(username);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().equals(Role.TEACHER)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Unauthorized access");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        User currentUser = userOptional.get();
        
        try {
            // Get teacher's courses
            List<Course> teacherCourses = courseRepository.findByTeacherId(currentUser.getId());
            
            // Calculate stats
            int totalCourses = teacherCourses.size();
            int totalStudents = teacherCourses.stream()
                .mapToInt(course -> course.getEnrolledUserIds() != null ? course.getEnrolledUserIds().size() : 0)
                .sum();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCourses", totalCourses);
            stats.put("totalStudents", totalStudents);
            stats.put("completionRate", 85); // Mock data
            stats.put("activeCourses", totalCourses);
            stats.put("newEnrollments", 12); // Mock data
            stats.put("averageRating", "4.5");
            stats.put("monthlyRevenue", "$2,450");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error loading stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/api/notifications")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> userOptional = userRepository.findByEmail(username);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().equals(Role.TEACHER)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Unauthorized access");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        User currentUser = userOptional.get();
        
        try {
            List<Notification> notifications = notificationService.getNotificationsForTeacher(currentUser.getId());
            long unreadCount = notificationService.getUnreadCountForTeacher(currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notifications", notifications);
            response.put("unreadCount", unreadCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error loading notifications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/api/students")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStudentsInCourses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> userOptional = userRepository.findByEmail(username);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().equals(Role.TEACHER)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Unauthorized access");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        User currentUser = userOptional.get();
        
        try {
            // Get teacher's courses
            List<Course> teacherCourses = courseRepository.findByTeacherId(currentUser.getId());
            
            // Get all students (role = STUDENT) enrolled in teacher's courses
            List<User> students = teacherCourses.stream()
                .flatMap(course -> {
                    if (course.getEnrolledUserIds() != null) {
                        return course.getEnrolledUserIds().stream()
                            .map(userId -> userRepository.findById(userId).orElse(null))
                            .filter(user -> user != null && user.getRole() == Role.STUDENT); // Only STUDENT role
                    }
                    return Stream.empty();
                })
                .distinct()
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("students", students);
            response.put("totalStudents", students.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error loading students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/notifications/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markNotificationAsRead(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> userOptional = userRepository.findByEmail(username);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().equals(Role.TEACHER)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Unauthorized access");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            notificationService.markAsRead(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification marked as read");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error marking notification as read: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/api/notifications/{id}/download/{index}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable String id, @PathVariable int index) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Optional<User> userOptional = userRepository.findByEmail(username);
            
            if (userOptional.isEmpty() || !userOptional.get().getRole().equals(Role.TEACHER)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Notification notification = notificationService.getNotificationById(id);
            if (notification == null || notification.getAttachmentUrls() == null || 
                index >= notification.getAttachmentUrls().size()) {
                return ResponseEntity.notFound().build();
            }

            String fileUrl = notification.getAttachmentUrls().get(index);
            String fileName = notification.getAttachmentNames().get(index);
            
            // Convert URL to file path
            String filePath = "src/main/resources/static" + fileUrl;
            Path path = Paths.get(filePath);
            
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(path);
            
            // Encode filename to handle Unicode characters
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName)
                    .header("Content-Type", "application/octet-stream")
                    .body(fileContent);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/notifications/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNotificationDetail(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> userOptional = userRepository.findByEmail(username);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().equals(Role.TEACHER)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Unauthorized access");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Notification notification = notificationService.getNotificationById(id);
            if (notification == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Thông báo không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notification", notification);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error loading notification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/api/courses")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTeacherCourses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> userOptional = userRepository.findByEmail(username);
        
        if (userOptional.isEmpty() || !userOptional.get().getRole().equals(Role.TEACHER)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Unauthorized access");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        User currentUser = userOptional.get();
        
        try {
            System.out.println("👨‍🏫 Teacher Email: " + currentUser.getEmail());
            System.out.println("👨‍🏫 Teacher ID: " + currentUser.getId());
            
            // Debug: Check all courses in database
            List<Course> allCourses = courseRepository.findAll();
            System.out.println("🔍 Total courses in database: " + allCourses.size());
            
            for (Course course : allCourses) {
                System.out.println("🔍 Course: " + course.getTitle() + 
                    " | teacherId: '" + course.getTeacherId() + "'" +
                    " | createdByUserId: '" + course.getCreatedByUserId() + "'");
            }
            
            // Get teacher's courses
            List<Course> teacherCourses = courseRepository.findByTeacherId(currentUser.getId());
            System.out.println("📚 Found " + teacherCourses.size() + " courses for teacher ID: " + currentUser.getId());
            
            // Also try searching by createdByUserId
            List<Course> coursesByCreator = courseRepository.findByCreatedByUserId(currentUser.getId());
            System.out.println("📚 Found " + coursesByCreator.size() + " courses by createdByUserId: " + currentUser.getId());
            
            // Use courses created by this user if teacherId search returns empty
            if (teacherCourses.isEmpty() && !coursesByCreator.isEmpty()) {
                System.out.println("🔄 Using courses found by createdByUserId instead");
                teacherCourses = coursesByCreator;
            }
            
            // Log each course
            for (Course course : teacherCourses) {
                System.out.println("📖 Course: " + course.getTitle() + " | Teacher ID in DB: " + course.getTeacherId());
            }
            
            // Create course data with additional info
            List<Map<String, Object>> courseData = teacherCourses.stream().map(course -> {
                Map<String, Object> courseInfo = new HashMap<>();
                courseInfo.put("id", course.getId());
                courseInfo.put("title", course.getTitle());
                courseInfo.put("description", course.getDescription());
                courseInfo.put("status", course.getStatus());
                courseInfo.put("image", course.getImage());
                courseInfo.put("level", course.getLevel());
                courseInfo.put("instructorName", course.getInstructorName());
                courseInfo.put("duration", course.getDuration());
                courseInfo.put("prerequisites", course.getPrerequisites());
                courseInfo.put("objectives", course.getObjectives());
                
                // Count enrolled students
                int enrolledCount = course.getEnrolledUserIds() != null ? course.getEnrolledUserIds().size() : 0;
                courseInfo.put("enrolledCount", enrolledCount);
                
                // Count lessons
                List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
                courseInfo.put("lessonCount", lessons.size());
                
                return courseInfo;
            }).collect(Collectors.toList());
            
            System.out.println("📊 Returning " + courseData.size() + " courses to frontend");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courses", courseData);
            response.put("totalCourses", teacherCourses.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in getTeacherCourses: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error loading courses: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Simplified API endpoints for teacher statistics
    @GetMapping("/api/statistics/courses")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getTeacherCourseStatistics() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.getRole().equals(Role.TEACHER)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String currentUserId = currentUser.getId();
            List<Course> teacherCourses = courseService.getCoursesByInstructor(currentUserId);
            
            List<Map<String, Object>> courseStatsList = new ArrayList<>();
            
            for (Course course : teacherCourses) {
                Map<String, Object> courseStats = new HashMap<>();
                courseStats.put("courseId", course.getId());
                courseStats.put("title", course.getTitle());
                
                // Get rating statistics if available
                try {
                    Map<String, Object> ratingStats = commentService.getCourseRatingStats(course.getId());
                    courseStats.put("averageRating", ratingStats.get("averageRating"));
                    courseStats.put("totalComments", ratingStats.get("totalComments"));
                } catch (Exception e) {
                    courseStats.put("averageRating", 0.0);
                    courseStats.put("totalComments", 0);
                }
                
                // Get enrolled students count from course's enrolledUserIds
                int enrolledStudents = course.getEnrolledUserIds() != null ? course.getEnrolledUserIds().size() : 0;
                courseStats.put("enrolledStudents", enrolledStudents);
                
                // Set default completion stats (can be enhanced later)
                courseStats.put("completed50", 0);
                courseStats.put("completed75", 0);
                courseStats.put("completed100", 0);
                
                courseStatsList.add(courseStats);
            }
            
            return ResponseEntity.ok(courseStatsList);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/api/statistics/comments")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getTeacherComments() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.getRole().equals(Role.TEACHER)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String currentUserId = currentUser.getId();
            List<Course> teacherCourses = courseService.getCoursesByInstructor(currentUserId);
            
            List<Map<String, Object>> allComments = new ArrayList<>();
            
            for (Course course : teacherCourses) {
                try {
                    List<Comment> courseComments = commentService.getCommentsByCourseId(course.getId());
                    
                    for (Comment comment : courseComments) {
                        if (comment.getActive()) {
                            Map<String, Object> commentData = new HashMap<>();
                            commentData.put("id", comment.getId());
                            commentData.put("content", comment.getContent());
                            commentData.put("rating", comment.getRating());
                            commentData.put("createdAt", comment.getCreatedAt());
                            commentData.put("courseId", course.getId());
                            commentData.put("courseName", course.getTitle());
                            
                            // Get user name
                            try {
                                User user = userRepository.findById(comment.getUserId()).orElse(null);
                                commentData.put("userName", user != null ? user.getName() : "Unknown User");
                                commentData.put("userRole", user != null ? user.getRole().toString() : "STUDENT");
                            } catch (Exception e) {
                                commentData.put("userName", "Unknown User");
                                commentData.put("userRole", "STUDENT");
                            }
                            
                            allComments.add(commentData);
                        }
                    }
                } catch (Exception e) {
                    // Skip if comments can't be loaded for this course
                    continue;
                }
            }
            
            // Sort by creation date (newest first)
            allComments.sort((a, b) -> {
                try {
                    Date dateA = (Date) a.get("createdAt");
                    Date dateB = (Date) b.get("createdAt");
                    return dateB.compareTo(dateA);
                } catch (Exception e) {
                    return 0;
                }
            });
            
            return ResponseEntity.ok(allComments);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 