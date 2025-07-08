package com.elearning.elearning_backend.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
import com.elearning.elearning_backend.Model.Category;
import com.elearning.elearning_backend.Model.Category.CategoryType;
import com.elearning.elearning_backend.Model.Comment;
import com.elearning.elearning_backend.Model.Course;
import com.elearning.elearning_backend.Model.Notification;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.UserRepository;
import com.elearning.elearning_backend.Service.CategoryService;
import com.elearning.elearning_backend.Service.CommentService;
import com.elearning.elearning_backend.Service.CourseService;
import com.elearning.elearning_backend.Service.LessonProgressService;
import com.elearning.elearning_backend.Service.NotificationService;
import com.elearning.elearning_backend.Service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final CourseService courseService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final CommentService commentService;
    private final LessonProgressService lessonProgressService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole());
        
        // Get recent users for dashboard
        List<User> recentUsers = userRepository.findAll();
        model.addAttribute("recentUsers", recentUsers);
        
        return "pages/dashboard_admin";
    }

    @GetMapping("/users")
    public String users(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        List<User> allUsers = userRepository.findAll();
        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole());
        model.addAttribute("users", allUsers);
        return "admin/users";
    }

    @GetMapping("/courses")
    public String courses(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole());
        
        // Add categories for course creation/editing dropdowns
        List<Category> skillCategories = categoryService.getCategoriesByType(CategoryType.SKILL);
        List<Category> jobRoles = categoryService.getCategoriesByType(CategoryType.ROLE);
        
        model.addAttribute("skillCategories", skillCategories);
        model.addAttribute("jobRoles", jobRoles);
        
        return "admin/courses";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole());
        return "admin/reports";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole());
        return "admin/settings";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole());
        
        // Lấy danh mục từ database thay vì enum
        List<Category> skillCategories = categoryService.getCategoriesByType(CategoryType.SKILL);
        List<Category> jobRoles = categoryService.getCategoriesByType(CategoryType.ROLE);
        
        model.addAttribute("skillCategories", skillCategories);
        model.addAttribute("jobRoles", jobRoles);
        
        // Đếm số khóa học cho mỗi skill category
        Map<Category, Long> skillCategoryCounts = new HashMap<>();
        for (Category category : skillCategories) {
            long count = categoryService.getCourseCountByCategory(category);
            skillCategoryCounts.put(category, count);
        }
        
        // Đếm số khóa học cho mỗi job role
        Map<Category, Long> jobRoleCounts = new HashMap<>();
        for (Category role : jobRoles) {
            long count = categoryService.getCourseCountByCategory(role);
            jobRoleCounts.put(role, count);
        }
        
        model.addAttribute("skillCategoryCounts", skillCategoryCounts);
        model.addAttribute("jobRoleCounts", jobRoleCounts);
        
        // Tính tổng số khóa học
        long totalCourses = courseService.getAllCourses().size();
        model.addAttribute("totalCourses", totalCourses);
        
        return "admin/categories";
    }

    @GetMapping("/messages")
    public String messages(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole());
        return "admin/messages";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole());
        return "admin/profile";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole());
        return "admin/statistics";
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
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
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
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
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
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
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
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
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
            String newFilename = "admin_" + currentUser.getId() + "_" + UUID.randomUUID().toString() + fileExtension;

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

    @PostMapping("/api/notifications/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createNotification(
            @RequestParam("title") String title,
            @RequestParam("message") String message,
            @RequestParam(value = "toUserId", required = false) String toUserId,
            @RequestParam("priority") String priority,
            @RequestParam("type") String type,
            @RequestParam(value = "attachmentUrls", required = false) List<String> attachmentUrls,
            @RequestParam(value = "attachmentNames", required = false) List<String> attachmentNames,
            @RequestParam(value = "attachmentTypes", required = false) List<String> attachmentTypes,
            @RequestParam(value = "attachmentSizes", required = false) List<Long> attachmentSizes) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.badRequest().body(response);
            }

            // Initialize attachment lists if they are null
            if (attachmentUrls == null) attachmentUrls = new ArrayList<>();
            if (attachmentNames == null) attachmentNames = new ArrayList<>();
            if (attachmentTypes == null) attachmentTypes = new ArrayList<>();
            if (attachmentSizes == null) attachmentSizes = new ArrayList<>();

            // Validate that all attachment lists have the same size if any attachments exist
            if (!attachmentUrls.isEmpty()) {
                int expectedSize = attachmentUrls.size();
                if (attachmentNames.size() != expectedSize || 
                    attachmentTypes.size() != expectedSize || 
                    attachmentSizes.size() != expectedSize) {
                    response.put("success", false);
                    response.put("message", "Attachment data is inconsistent");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            if (toUserId != null && !toUserId.isEmpty()) {
                // Send to specific teacher
                notificationService.createNotification(title, message, currentUser.getId(), toUserId, priority, type, 
                    attachmentUrls, attachmentNames, attachmentTypes, attachmentSizes);
            } else {
                // Broadcast to all teachers
                notificationService.createBroadcastNotification(title, message, currentUser.getId(), priority, type,
                    attachmentUrls, attachmentNames, attachmentTypes, attachmentSizes);
            }

            response.put("success", true);
            response.put("message", "Notification created and sent successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace for debugging
            response.put("success", false);
            response.put("message", "Error creating notification: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/users/recent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecentUsers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.badRequest().body(response);
            }

            List<User> allUsers = userRepository.findAll();
            
            response.put("success", true);
            response.put("users", allUsers);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/notifications/{id}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Delete notification request received for ID: " + id);
            
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                System.out.println("Unauthorized access - User: " + (currentUser != null ? currentUser.getEmail() : "null") + 
                                 ", Role: " + (currentUser != null ? currentUser.getRole() : "null"));
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.status(401).body(response);
            }

            // Check if notification exists
            Notification notification = notificationService.getNotificationById(id);
            if (notification == null) {
                System.out.println("Notification not found with ID: " + id);
                response.put("success", false);
                response.put("message", "Thông báo không tồn tại");
                return ResponseEntity.status(404).body(response);
            }

            System.out.println("Deleting notification with ID: " + id);
            notificationService.deleteNotification(id);
            System.out.println("Notification deleted successfully");

            response.put("success", true);
            response.put("message", "Thông báo đã được xóa thành công");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(response);

        } catch (Exception e) {
            System.out.println("Error deleting notification: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/notifications/recent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecentNotifications() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.badRequest().body(response);
            }

            List<Notification> allNotifications = notificationService.getAllNotifications();
            
            // Sort by creation date (newest first) and take first 10
            List<Notification> recentNotifications = allNotifications.stream()
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList());

            response.put("success", true);
            response.put("notifications", recentNotifications);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.badRequest().body(response);
            }

            // Get system statistics
            long totalUsers = userRepository.count();
            long totalAdmins = userRepository.countByRole(Role.ADMIN);
            long totalTeachers = userRepository.countByRole(Role.TEACHER);
            long totalStudents = userRepository.countByRole(Role.STUDENT);
            long totalCourses = courseService.getAllCourses().size();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("totalAdmins", totalAdmins);
            stats.put("totalTeachers", totalTeachers);
            stats.put("totalStudents", totalStudents);
            stats.put("totalCourses", totalCourses);
            stats.put("activeUsers", totalUsers);
            stats.put("newRegistrations", totalUsers); // Placeholder
            stats.put("totalNotifications", notificationService.getAllNotifications().size());

            response.put("success", true);
            response.put("stats", stats);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/notifications/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNotificationDetail(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.badRequest().body(response);
            }

            Notification notification = notificationService.getNotificationById(id);
            if (notification == null) {
                response.put("success", false);
                response.put("message", "Thông báo không tồn tại");
                return ResponseEntity.badRequest().body(response);
            }

            response.put("success", true);
            response.put("notification", notification);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/notifications/{id}/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateNotification(
            @PathVariable String id,
            @RequestParam("title") String title,
            @RequestParam("message") String message,
            @RequestParam("priority") String priority,
            @RequestParam("type") String type) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.badRequest().body(response);
            }

            Notification updatedNotification = notificationService.updateNotification(id, title, message, priority, type);
            if (updatedNotification == null) {
                response.put("success", false);
                response.put("message", "Không thể cập nhật thông báo");
                return ResponseEntity.badRequest().body(response);
            }

            response.put("success", true);
            response.put("message", "Cập nhật thông báo thành công");
            response.put("notification", updatedNotification);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/teachers")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTeachers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.badRequest().body(response);
            }

            List<User> teachers = userRepository.findByRole(Role.TEACHER);

            response.put("success", true);
            response.put("teachers", teachers);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/notifications/{id}/download/{index}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable String id, @PathVariable int index) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Notification notification = notificationService.getNotificationById(id);
            if (notification == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (notification.getAttachmentUrls() == null || 
                index >= notification.getAttachmentUrls().size() || 
                index < 0) {
                return ResponseEntity.notFound().build();
            }

            String fileName = notification.getAttachmentUrls().get(index);
            Path filePath = Paths.get("uploads/notifications/" + fileName);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            String originalName = notification.getAttachmentNames() != null && 
                                index < notification.getAttachmentNames().size() ? 
                                notification.getAttachmentNames().get(index) : fileName;

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + originalName + "\"")
                    .body(fileContent);

        } catch (Exception e) {
            System.err.println("Error downloading attachment: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Statistics API endpoints
    @GetMapping("/api/statistics/courses")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCourseStatistics() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
            }

            List<Course> allCourses = courseService.getAllCourses();
            List<Map<String, Object>> courseStats = new ArrayList<>();

            for (Course course : allCourses) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("courseId", course.getId());
                stats.put("courseTitle", course.getTitle());
                stats.put("instructorName", course.getInstructorName());
                
                // Get rating and comment stats
                Map<String, Object> commentStats = commentService.getCommentStatsByCourse(course.getId());
                stats.put("averageRating", commentStats.getOrDefault("averageRating", 0.0));
                stats.put("totalComments", commentStats.getOrDefault("totalComments", 0));
                
                // Get enrollment count
                int enrolledCount = course.getEnrolledUserIds() != null ? course.getEnrolledUserIds().size() : 0;
                stats.put("enrolledStudents", enrolledCount);
                
                // Get completion stats
                int totalLessons = course.getLessons() != null ? course.getLessons().size() : 0;
                Map<String, Object> completionStats = lessonProgressService.getDetailedCourseStats(course.getId(), totalLessons);
                stats.putAll(completionStats);
                
                courseStats.add(stats);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courses", courseStats);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error getting course statistics: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Error retrieving statistics"));
        }
    }
    
    @GetMapping("/api/statistics/comments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCommentStatistics(
            @RequestParam(value = "search", required = false) String search) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
            }

            List<Comment> comments = commentService.searchComments(search);
            long totalComments = commentService.getTotalCommentsCount();

            // Enrich comments with course information
            List<Map<String, Object>> enrichedComments = comments.stream().map(comment -> {
                Map<String, Object> commentData = new HashMap<>();
                commentData.put("id", comment.getId());
                commentData.put("content", comment.getContent());
                commentData.put("rating", comment.getRating());
                commentData.put("createdAt", comment.getCreatedAt());
                commentData.put("userId", comment.getUserId());
                commentData.put("courseId", comment.getCourseId());
                
                // Get course name
                try {
                    Course course = courseService.getCourseById(comment.getCourseId());
                    commentData.put("courseName", course != null ? course.getTitle() : "Unknown Course");
                } catch (Exception e) {
                    commentData.put("courseName", "Unknown Course");
                }
                
                // Get user name
                try {
                    User user = userRepository.findById(comment.getUserId()).orElse(null);
                    commentData.put("userName", user != null ? user.getName() : "Unknown User");
                } catch (Exception e) {
                    commentData.put("userName", "Unknown User");
                }
                
                return commentData;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("comments", enrichedComments);
            response.put("totalComments", totalComments);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error getting comment statistics: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Error retrieving comments"));
        }
    }
    
    @PostMapping("/api/statistics/comments/{commentId}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCommentFromStats(@PathVariable String commentId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
            }

            commentService.deleteComment(commentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Comment deleted successfully");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error deleting comment: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Error deleting comment"));
        }
    }
} 