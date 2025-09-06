package com.elearning.elearning_backend.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.elearning.elearning_backend.Model.Course;
import com.elearning.elearning_backend.Model.Message;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.MessageRepository;
import com.elearning.elearning_backend.Repository.UserRepository;
import com.elearning.elearning_backend.Service.CourseService;

import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudentController {
    
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final MessageRepository messageRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    private User getCurrentUserFromSession(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    @GetMapping("/auth/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUserInfo() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/student/enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Course>> getUserEnrollments() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Course> enrolledCourses = courseService.getEnrolledCourses(user.getId());
        return ResponseEntity.ok(enrolledCourses);
    }

    // API for floating chat - get enrolled courses
    @GetMapping("/student/enrolled-courses")
    public ResponseEntity<?> getEnrolledCourses(HttpSession session) {
        try {
            User currentUser = getCurrentUserFromSession(session);
            if (currentUser == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            List<Course> enrolledCourses = courseService.getEnrolledCourses(currentUser.getId());
            
            // Convert to format expected by floating chat
            List<Map<String, Object>> courseData = new ArrayList<>();
            for (Course course : enrolledCourses) {
                Map<String, Object> courseInfo = new HashMap<>();
                courseInfo.put("id", course.getId());
                courseInfo.put("title", course.getTitle());
                courseInfo.put("teacherId", course.getCreatedByUserId()); // Use createdByUserId as teacherId
                courseInfo.put("teacherName", course.getInstructorName());
                courseInfo.put("description", course.getDescription());
                courseInfo.put("level", course.getLevel());
                courseInfo.put("image", course.getImage());
                courseData.add(courseInfo);
            }
            
            return ResponseEntity.ok(courseData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error loading courses: " + e.getMessage());
        }
    }

    // API for floating chat - get course teacher info
    @GetMapping("/courses/{courseId}/teacher")
    public ResponseEntity<?> getCourseTeacher(@PathVariable String courseId, HttpSession session) {
        try {
            User currentUser = getCurrentUserFromSession(session);
            if (currentUser == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            Course course = courseService.getCourseById(courseId);
            if (course == null) {
                return ResponseEntity.notFound().build();
            }
            
            User teacher = userRepository.findById(course.getTeacherId()).orElse(null);
            if (teacher == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("course", course);
            result.put("teacher", teacher);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error loading teacher info: " + e.getMessage());
        }
    }

    // API for floating chat - send message
    @PostMapping("/chat/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> messageData, HttpSession session) {
        try {
            User currentUser = getCurrentUserFromSession(session);
            if (currentUser == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            // Create message
            Message message = new Message();
            message.setSenderId(currentUser.getId().toString());
            message.setReceiverId(messageData.get("receiverId").toString());
            message.setContent(messageData.get("content").toString());
            message.setCourseName(messageData.get("courseName").toString());
            message.setCourseId(messageData.get("courseId").toString());
            message.setSenderName(currentUser.getName());
            message.setReceiverName(messageData.get("receiverName").toString());
            message.setSenderRole(currentUser.getRole().name());
            message.setReceiverRole(messageData.get("receiverRole").toString());
            message.setType(Message.MessageType.CHAT);
            message.setIsRead(false);
            
            Message savedMessage = messageRepository.save(message);
            
            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending message: " + e.getMessage());
        }
    }

    // API for floating chat - get conversation
    @GetMapping("/api/student/conversation")
    public ResponseEntity<?> getConversation(@RequestParam Long userId1, 
                                           @RequestParam Long userId2, 
                                           @RequestParam Long courseId,
                                           HttpSession session) {
        try {
            User currentUser = getCurrentUserFromSession(session);
            if (currentUser == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            // Get messages between two users for a specific course
            List<Message> messages = messageRepository.findConversationBetweenUsersInCourse(
                userId1.toString(), userId2.toString(), courseId.toString()
            );
            
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error loading conversation: " + e.getMessage());
        }
    }

    // API for floating chat - get conversation with teacher
    @GetMapping("/student/conversations/{teacherId}")
    public ResponseEntity<?> getConversationWithTeacher(@PathVariable String teacherId, HttpSession session) {
        try {
            User currentUser = getCurrentUserFromSession(session);
            if (currentUser == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            // Get all conversations for this user and filter by teacherId
            List<Message> allMessages = messageRepository.findAllConversationsForUser(currentUser.getId());
            
            // Filter messages between current user and teacher
            List<Message> messages = new ArrayList<>();
            for (Message message : allMessages) {
                if ((message.getSenderId().equals(currentUser.getId()) && message.getReceiverId().equals(teacherId)) ||
                    (message.getSenderId().equals(teacherId) && message.getReceiverId().equals(currentUser.getId()))) {
                    messages.add(message);
                }
            }
            
            // Sort by timestamp
            messages.sort((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));
            
            // Convert to format expected by floating chat
            List<Map<String, Object>> messageData = new ArrayList<>();
            for (Message message : messages) {
                Map<String, Object> messageInfo = new HashMap<>();
                messageInfo.put("id", message.getId());
                messageInfo.put("senderId", message.getSenderId());
                messageInfo.put("receiverId", message.getReceiverId());
                messageInfo.put("content", message.getContent());
                messageInfo.put("timestamp", message.getCreatedAt());
                messageInfo.put("senderName", message.getSenderName());
                messageData.add(messageInfo);
            }
            
            return ResponseEntity.ok(messageData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error loading conversation: " + e.getMessage());
        }
    }

    @PostMapping("/student/courses/{courseId}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> enrollInCourseByPath(@PathVariable String courseId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        
        try {
            // Check if already enrolled
            if (courseService.isUserEnrolledInCourse(courseId, user.getId())) {
                return ResponseEntity.badRequest().body("Already enrolled in this course");
            }
            
            // Enroll user in course
            courseService.enrollUserInCourse(courseId, user.getId());
            
            return ResponseEntity.ok("Successfully enrolled in course");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to enroll: " + e.getMessage());
        }
    }

    @PostMapping("/student/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> enrollInCourse(@RequestBody EnrollmentRequest request) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        
        try {
            // Check if already enrolled
            if (courseService.isUserEnrolledInCourse(request.getCourseId(), user.getId())) {
                return ResponseEntity.badRequest().body("Already enrolled in this course");
            }
            
            // Enroll user in course
            courseService.enrollUserInCourse(request.getCourseId(), user.getId());
            
            return ResponseEntity.ok("Successfully enrolled in course");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to enroll: " + e.getMessage());
        }
    }

    @GetMapping("/student/courses/{courseId}/check-enrollment")
    public ResponseEntity<Map<String, Boolean>> checkEnrollment(@PathVariable String courseId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Collections.singletonMap("enrolled", false));
        }
        
        boolean isEnrolled = courseService.isUserEnrolledInCourse(courseId, user.getId());
        return ResponseEntity.ok(Collections.singletonMap("enrolled", isEnrolled));
    }

    @Data
    public static class EnrollmentRequest {
        private String courseId;
    }

    // API for floating chat - get unread messages count
    @GetMapping("/student/unread-count")
    public ResponseEntity<?> getUnreadCount(HttpSession session) {
        try {
            User currentUser = getCurrentUserFromSession(session);
            if (currentUser == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            Long unreadCount = messageRepository.countUnreadMessagesForUser(currentUser.getId());
            return ResponseEntity.ok(unreadCount);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error loading unread count: " + e.getMessage());
        }
    }
    
    // API for floating chat - mark messages as read
    @PostMapping("/student/mark-read/{senderId}")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable String senderId, HttpSession session) {
        try {
            User currentUser = getCurrentUserFromSession(session);
            if (currentUser == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            // Get all unread messages from this sender
            List<Message> unreadMessages = messageRepository.findUnreadMessagesForUser(currentUser.getId());
            
            // Mark messages from specific sender as read
            for (Message message : unreadMessages) {
                if (message.getSenderId().equals(senderId)) {
                    message.setIsRead(true);
                    messageRepository.save(message);
                }
            }
            
            return ResponseEntity.ok("Messages marked as read");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error marking messages as read: " + e.getMessage());
        }
    }
} 