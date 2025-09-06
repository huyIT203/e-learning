package com.elearning.elearning_backend.Controller;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.elearning.elearning_backend.Model.Lesson;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.UserRepository;
import com.elearning.elearning_backend.Service.LessonService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                String email = auth.getName();
                return userRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            System.out.println("❌ Error getting current user: " + e.getMessage());
        }
        return null;
    }

    @PostMapping("/courses/{courseId}/lessons")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> createLesson(@PathVariable String courseId, @RequestBody Lesson lesson) {
        try {
            User currentUser = getCurrentUser();
            System.out.println("🟢 Creating lesson for course: " + courseId);
            System.out.println("🟢 Current user: " + (currentUser != null ? currentUser.getEmail() + " (" + currentUser.getRole() + ")" : "null"));
            System.out.println("🟢 Lesson data received:");
            System.out.println("  - Title: " + lesson.getTitle());
            System.out.println("  - Description: " + lesson.getDescription());
            System.out.println("  - Content HTML: " + (lesson.getContentHtml() != null ? lesson.getContentHtml().substring(0, Math.min(100, lesson.getContentHtml().length())) + "..." : "null"));
            System.out.println("  - Video URL: " + lesson.getVideoUrl());
            
            // Validate required fields
            if (lesson.getTitle() == null || lesson.getTitle().trim().isEmpty()) {
                System.err.println("❌ Lesson title is required");
                return ResponseEntity.badRequest().body("{\"error\": \"Tiêu đề bài học là bắt buộc\"}");
            }
            
            // Set the creator
            if (currentUser != null) {
                lesson.setCreatedByUserId(currentUser.getId());
            }
            
            Lesson createdLesson = lessonService.createLesson(courseId, lesson);
            
            System.out.println("🟢 Lesson created successfully with ID: " + createdLesson.getId());
            return ResponseEntity.ok(createdLesson);
        } catch (Exception e) {
            System.err.println("❌ Error creating lesson: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Lỗi tạo bài học: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<List<Lesson>> getLessonsByCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(lessonService.getLessonsByCourseId(courseId));
    }

    @GetMapping("/lessons/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable String id) {
        return ResponseEntity.ok(lessonService.getLessonById(id).orElseThrow());
    }

    @PutMapping("/lessons/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Lesson> updateLesson(@PathVariable String id, @RequestBody Lesson lesson) {
        return ResponseEntity.ok(lessonService.updateLesson(id, lesson));
    }

    @DeleteMapping("/lessons/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLesson(@PathVariable String id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
    
    // Endpoint để tạo lesson mẫu với nội dung giống như trong hình
    @PostMapping("/courses/{courseId}/lessons/sample")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Lesson> createSampleLesson(@PathVariable String courseId) {
        String sampleContentHtml = """
            <div class="lesson-content">
                <h1>1.1.1 Primitive data types</h1>
                
                <p>In the previous part of the course, you got to know some primitive data types.</p>
                
                <p>In practice, only <code>number</code>, <code>boolean</code> and <code>string</code> types are actually used. Whether a type is a certain value can be checked with the <code>typeof</code> command.</p>
                
                <p>With this command, we can also test not just a particular value, but a variable (or more precisely, what is placed inside it).</p>
                
                <p>Try to execute the following commands in the console and observe the results. Furthermore, it would be good to try to test every piece of sample code that we discuss.</p>
                
                <div class="code-block">
                    <div class="code-header">
                        <span class="code-title">Console</span>
                        <button class="copy-btn">📋</button>
                    </div>
                    <pre><code>console.log(typeof 2.5); // -> number
console.log(typeof "one two three"); // -> string  
console.log(typeof false); // -> boolean</code></pre>
                </div>
                
                <p>Values can be placed in variables – we can store them there or perform actions on them (e.g., divide numbers or merge strings).</p>
                
                <div class="code-block">
                    <div class="code-header">
                        <span class="code-title">Console</span>
                        <button class="copy-btn">📋</button>
                    </div>
                    <pre><code>let nr = 2.5;
nr = nr / 2;
console.log(typeof nr); // -> number</code></pre>
                </div>
                
                <p>If we have not explicitly assigned any value to a declared variable, it contains an undefined value by default. The undefined value is also.</p>
            </div>
            """;
        
        Lesson sampleLesson = Lesson.builder()
                .title("1.1.1 Primitive data types")
                .description("In the previous part of the course, you got to know some primitive data types.")
                .contentHtml(sampleContentHtml)
                .videoUrl("https://www.youtube.com/watch?v=sample")
                .courseId(courseId)
                .build();
        
        return ResponseEntity.ok(lessonService.createLesson(courseId, sampleLesson));
    }
    
    @GetMapping("/courses/{courseId}/lessons/create")
    public String showCreateForm(@PathVariable String courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "shared/lesson_create";
    }

    @PostMapping("/lessons/{lessonId}/upload-body")
    public ResponseEntity<?> uploadLessonBody(@PathVariable String lessonId,
                                              @RequestParam("file") MultipartFile file) {
        try {

            String uploadDir = "uploads/lesson-body/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // 2. Tạo tên file
            String fileName = lessonId + "-" + file.getOriginalFilename();
            String filePath = uploadDir + fileName;
            File Dir = new File("uploads/lesson-body");
            if (!Dir.exists()) {
                Dir.mkdirs();
            }
            // 3. Lưu file vào disk
            file.transferTo(new File(filePath));

            // 4. Cập nhật path vào lesson
            Lesson lesson = lessonService.getLessonById(lessonId).orElseThrow();
            lesson.setBodyPath(filePath);
            lessonService.updateLesson(lessonId, lesson);

            return ResponseEntity.ok("Upload thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    
}





