package com.elearning.elearning_backend.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.elearning.elearning_backend.Enum.Role;
import com.elearning.elearning_backend.Model.Course;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.UserRepository;
import com.elearning.elearning_backend.Service.CourseService;
import com.elearning.elearning_backend.Service.LessonProgressService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentViewController {
    
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final LessonProgressService lessonProgressService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.STUDENT) {
            return "redirect:/login";
        }
        
        model.addAttribute("currentUser", currentUser);
        
        // Get real enrolled courses data
        List<Course> enrolledCourses = courseService.getEnrolledCourses(currentUser.getId());
        List<Map<String, Object>> enrolledCoursesData = new ArrayList<>();
        
        int totalEnrolled = enrolledCourses.size();
        int inProgress = 0;
        int completed = 0;
        int certificates = 0;
        
        for (Course course : enrolledCourses) {
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("id", course.getId());
            courseData.put("title", course.getTitle());
            courseData.put("jobRole", course.getJobRole());
            courseData.put("skillCategory", course.getSkillCategory());
            courseData.put("level", course.getLevel() != null ? course.getLevel().name() : "BEGINNER");
            courseData.put("instructor", course.getInstructorName());
            
            // Calculate progress based on lesson completion
            int totalLessons = course.getLessons().size();
            int completedLessons = 0;
            
            if (totalLessons > 0) {
                completedLessons = lessonProgressService.getCompletedLessonsCount(currentUser.getId(), course.getId());
            }
            
            int progressPercentage = totalLessons > 0 ? (completedLessons * 100 / totalLessons) : 0;
            courseData.put("progress", progressPercentage);
            
            // Determine course status
            String status;
            if (progressPercentage == 100) {
                status = "completed";
                completed++;
                certificates++; // Assume certificate is available for completed courses
            } else if (progressPercentage > 0) {
                status = "in-progress";
                inProgress++;
            } else {
                status = "not-started";
                inProgress++; // Count not-started as in-progress for stats
            }
            courseData.put("status", status);
            
            // Set default image URL (you can enhance this to store actual course images)
            courseData.put("imageUrl", "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=60");
            
            enrolledCoursesData.add(courseData);
        }
        
        model.addAttribute("enrolledCourses", enrolledCoursesData);
        
        // Add learning statistics
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalEnrolled", totalEnrolled);
        stats.put("inProgress", inProgress);
        stats.put("completed", completed);
        stats.put("certificates", certificates);
        model.addAttribute("learningStats", stats);
        
        return "student/dashboard";
    }

    @GetMapping("/courses")
    public String courses(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !currentUser.getRole().equals(Role.STUDENT)) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", currentUser);
        // Redirect to dashboard since courses page is now the dashboard
        return "redirect:/student/dashboard";
    }

    @GetMapping("/exams")
    public String exams(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !currentUser.getRole().equals(Role.STUDENT)) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", currentUser);
        // Temporary - just return the error page as a placeholder
        return "error";
    }

    @GetMapping("/results")
    public String results(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !currentUser.getRole().equals(Role.STUDENT)) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", currentUser);
        // Temporary - just return the error page as a placeholder
        return "error";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.STUDENT) {
            return "redirect:/login";
        }
        
        model.addAttribute("currentUser", currentUser);
        
        // Get enrolled courses for learning history
        List<Course> enrolledCourses = courseService.getEnrolledCourses(currentUser.getId());
        List<Map<String, Object>> learningHistory = new ArrayList<>();
        
        int totalCourses = enrolledCourses.size();
        int completedCourses = 0;
        int inProgressCourses = 0;
        int totalCertificates = 0;
        
        for (Course course : enrolledCourses) {
            Map<String, Object> historyItem = new HashMap<>();
            historyItem.put("id", course.getId());
            historyItem.put("title", course.getTitle());
            historyItem.put("instructor", course.getInstructorName());
            historyItem.put("instructorName", course.getInstructorName());
            historyItem.put("image", course.getImage());
            historyItem.put("level", course.getLevel());
            
            // Calculate progress
            int totalLessons = course.getLessons().size();
            int completedLessons = 0;
            
            if (totalLessons > 0) {
                completedLessons = lessonProgressService.getCompletedLessonsCount(currentUser.getId(), course.getId());
            }
            
            int progressPercentage = totalLessons > 0 ? (completedLessons * 100 / totalLessons) : 0;
            historyItem.put("progress", progressPercentage);
            
            // Determine status
            String status;
            if (progressPercentage == 100) {
                status = "completed";
                completedCourses++;
                totalCertificates++; // Assume certificate is available for completed courses
                historyItem.put("score", 95); // Mock score for completed courses
                historyItem.put("completedDate", "2024-01-15"); // Mock completion date
            } else if (progressPercentage > 0) {
                status = "in-progress";
                inProgressCourses++;
                historyItem.put("lastAccessed", "2024-01-20"); // Mock last access date
            } else {
                status = "not-started";
                inProgressCourses++; // Count not-started as in-progress for stats
                historyItem.put("enrolledDate", "2024-01-10"); // Mock enrollment date
            }
            historyItem.put("status", status);
            
            learningHistory.add(historyItem);
        }
        
        model.addAttribute("learningHistory", learningHistory);
        
        // Add profile statistics
        Map<String, Integer> profileStats = new HashMap<>();
        profileStats.put("totalCourses", totalCourses);
        profileStats.put("completedCourses", completedCourses);
        profileStats.put("inProgressCourses", inProgressCourses);
        profileStats.put("certificates", totalCertificates);
        model.addAttribute("profileStats", profileStats);
        
        return "student/profile";
    }

    @PostMapping("/profile/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            @RequestParam("dob") String dob,
            @RequestParam("gender") String gender,
            @RequestParam("facebookUrl") String facebookUrl,
            @RequestParam("githubUrl") String githubUrl,
            @RequestParam("bio") String bio,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.badRequest().body(response);
            }
            
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.STUDENT) {
                response.put("success", false);
                response.put("message", "User not found or not authorized");
                return ResponseEntity.badRequest().body(response);
            }

            // Update user information
            currentUser.setName(name);
            currentUser.setPhone(phone);
            currentUser.setDob(dob);
            currentUser.setGender(gender);
            currentUser.setFacebookUrl(facebookUrl);
            currentUser.setGithubUrl(githubUrl);
            currentUser.setBio(bio);

            // Handle avatar upload
            if (avatar != null && !avatar.isEmpty()) {
                try {
                    // Create uploads directory if it doesn't exist
                    Path uploadsDir = Paths.get("uploads/avatars");
                    if (!Files.exists(uploadsDir)) {
                        Files.createDirectories(uploadsDir);
                    }
                    
                    // Generate unique filename
                    String originalFilename = avatar.getOriginalFilename();
                    String fileExtension = originalFilename != null && originalFilename.contains(".") 
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".jpg";
                    String avatarFileName = UUID.randomUUID().toString() + fileExtension;
                    Path avatarPath = uploadsDir.resolve(avatarFileName);
                    
                    // Save file
                    Files.copy(avatar.getInputStream(), avatarPath);
                    currentUser.setAvatarUrl("/uploads/avatars/" + avatarFileName);
                } catch (IOException e) {
                    response.put("success", false);
                    response.put("message", "Failed to upload avatar: " + e.getMessage());
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Save updated user
            User updatedUser = userRepository.save(currentUser);

            // Prepare success response
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            
            // Create user data for response
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", updatedUser.getId());
            userData.put("name", updatedUser.getName());
            userData.put("email", updatedUser.getEmail());
            userData.put("phone", updatedUser.getPhone());
            userData.put("dob", updatedUser.getDob());
            userData.put("gender", updatedUser.getGender());
            userData.put("facebookUrl", updatedUser.getFacebookUrl());
            userData.put("githubUrl", updatedUser.getGithubUrl());
            userData.put("bio", updatedUser.getBio());
            userData.put("avatarUrl", updatedUser.getAvatarUrl());
            userData.put("role", updatedUser.getRole());
            
            response.put("user", userData);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update profile: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 