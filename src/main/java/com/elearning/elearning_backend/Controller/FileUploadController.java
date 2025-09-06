package com.elearning.elearning_backend.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;


    @PostMapping("/course-image")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> uploadCourseImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn file để upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "Chỉ chấp nhận file ảnh (JPG, PNG, GIF)");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "Kích thước file không được vượt quá 5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, "courses");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return success response with file URL
            String fileUrl = "/uploads/courses/" + uniqueFilename;
            response.put("success", true);
            response.put("message", "Upload ảnh thành công");
            response.put("fileUrl", fileUrl);
            response.put("fileName", uniqueFilename);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lưu file: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/notification-attachment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadNotificationAttachment(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn file để upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file size (max 10MB for attachments)
            if (file.getSize() > 10 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "Kích thước file không được vượt quá 10MB");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file type - allow common document and image types
            String contentType = file.getContentType();
            if (contentType == null || !isAllowedFileType(contentType)) {
                response.put("success", false);
                response.put("message", "Loại file không được hỗ trợ. Chỉ chấp nhận: PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, TXT, JPG, PNG, GIF");
                return ResponseEntity.badRequest().body(response);
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get("src/main/resources/static/uploads/notifications");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return success response with file info
            String fileUrl = "/uploads/notifications/" + uniqueFilename;
            response.put("success", true);
            response.put("message", "Upload file thành công");
            response.put("fileUrl", fileUrl);
            response.put("fileName", uniqueFilename);
            response.put("originalName", originalFilename);
            response.put("fileSize", file.getSize());
            response.put("fileType", contentType);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lưu file: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private boolean isAllowedFileType(String contentType) {
        return contentType.equals("application/pdf") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               contentType.equals("application/vnd.ms-excel") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
               contentType.equals("application/vnd.ms-powerpoint") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
               contentType.equals("text/plain") ||
               contentType.startsWith("image/");
    }
} 