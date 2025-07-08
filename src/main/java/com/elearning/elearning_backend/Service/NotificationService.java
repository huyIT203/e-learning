package com.elearning.elearning_backend.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.elearning.elearning_backend.Enum.Role;
import com.elearning.elearning_backend.Model.Notification;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.NotificationRepository;
import com.elearning.elearning_backend.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // Create notification from admin to specific teacher with attachments
    public Notification createNotification(String title, String message, String fromUserId, String toUserId, 
                                         String priority, String type, List<String> attachmentUrls, 
                                         List<String> attachmentNames, List<String> attachmentTypes, List<Long> attachmentSizes) {
        Notification notification = new Notification(title, message, fromUserId, toUserId, priority, type, 
                                                    attachmentUrls, attachmentNames, attachmentTypes, attachmentSizes);
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send email notification to specific teacher
        if (toUserId != null) {
            sendEmailNotification(savedNotification, toUserId);
        }
        
        return savedNotification;
    }
    
    // Create notification from admin to specific teacher (without attachments - backward compatibility)
    public Notification createNotification(String title, String message, String fromUserId, String toUserId, String priority, String type) {
        return createNotification(title, message, fromUserId, toUserId, priority, type, null, null, null, null);
    }
    
    // Create broadcast notification to all teachers with attachments
    public Notification createBroadcastNotification(String title, String message, String fromUserId, 
                                                   String priority, String type, List<String> attachmentUrls, 
                                                   List<String> attachmentNames, List<String> attachmentTypes, List<Long> attachmentSizes) {
        Notification notification = new Notification(title, message, fromUserId, null, priority, type, 
                                                    attachmentUrls, attachmentNames, attachmentTypes, attachmentSizes);
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send email notification to all teachers
        sendBroadcastEmailNotification(savedNotification);
        
        return savedNotification;
    }
    
    // Create broadcast notification to all teachers (without attachments - backward compatibility)
    public Notification createBroadcastNotification(String title, String message, String fromUserId, String priority, String type) {
        return createBroadcastNotification(title, message, fromUserId, priority, type, null, null, null, null);
    }
    
    // Send email notification to specific teacher
    private void sendEmailNotification(Notification notification, String toUserId) {
        try {
            Optional<User> teacherOpt = userRepository.findById(toUserId);
            if (teacherOpt.isPresent() && teacherOpt.get().getRole() == Role.TEACHER) {
                User teacher = teacherOpt.get();
                
                String subject = "Thông báo mới từ E-learning: " + notification.getTitle();
                String emailContent = buildEmailContent(notification);
                
                emailService.sendDirectHtmlEmail(teacher.getEmail(), subject, emailContent);
            }
        } catch (Exception e) {
            System.err.println("Error sending email notification: " + e.getMessage());
        }
    }
    
    // Send broadcast email notification to all teachers
    private void sendBroadcastEmailNotification(Notification notification) {
        try {
            List<User> teachers = userRepository.findByRole(Role.TEACHER);
            
            String subject = "Thông báo chung từ E-learning: " + notification.getTitle();
            String emailContent = buildEmailContent(notification);
            
            for (User teacher : teachers) {
                try {
                    emailService.sendDirectHtmlEmail(teacher.getEmail(), subject, emailContent);
                } catch (Exception e) {
                    System.err.println("Error sending email to " + teacher.getEmail() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending broadcast email notification: " + e.getMessage());
        }
    }
    
    // Build email content with attachment information
    private String buildEmailContent(Notification notification) {
        StringBuilder content = new StringBuilder();
        
        // Start with complete HTML email template
        content.append("<!DOCTYPE html>");
        content.append("<html lang='vi'>");
        content.append("<head>");
        content.append("<meta charset='UTF-8'>");
        content.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        content.append("<title>Thông báo từ E-learning</title>");
        content.append("</head>");
        content.append("<body style='margin: 0; padding: 0; font-family: Arial, Helvetica, sans-serif; background-color: #f4f4f4;'>");
        
        // Main container
        content.append("<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; box-shadow: 0 4px 8px rgba(0,0,0,0.1);'>");
        
        // Header
        content.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px 20px; text-align: center;'>");
        content.append("<h1 style='color: #ffffff; margin: 0; font-size: 28px; font-weight: 300; letter-spacing: 1px;'>");
        content.append("🎓 E-Learning System");
        content.append("</h1>");
        content.append("<p style='color: #ffffff; margin: 10px 0 0 0; opacity: 0.9; font-size: 16px;'>");
        content.append("Hệ thống học tập trực tuyến");
        content.append("</p>");
        content.append("</div>");
        
        // Content body
        content.append("<div style='padding: 40px 30px;'>");
        
        // Notification title
        content.append("<div style='text-align: center; margin-bottom: 30px;'>");
        content.append("<h2 style='color: #333333; margin: 0; font-size: 24px; font-weight: 600;'>");
        content.append("📢 ").append(notification.getTitle());
        content.append("</h2>");
        content.append("</div>");
        
        // Priority and type badges
        String priorityText = getPriorityText(notification.getPriority());
        String typeText = getTypeText(notification.getType());
        String priorityColor = getPriorityColor(notification.getPriority());
        
        content.append("<div style='text-align: center; margin-bottom: 30px;'>");
        content.append("<span style='background: ").append(priorityColor);
        content.append("; color: white; padding: 8px 16px; border-radius: 20px; font-size: 14px; font-weight: 500; margin-right: 10px; display: inline-block;'>");
        content.append("🚨 ").append(priorityText);
        content.append("</span>");
        content.append("<span style='background: #6c757d; color: white; padding: 8px 16px; border-radius: 20px; font-size: 14px; font-weight: 500; display: inline-block;'>");
        content.append("📋 ").append(typeText);
        content.append("</span>");
        content.append("</div>");
        
        // Message content
        content.append("<div style='background: #f8f9fa; padding: 25px; border-radius: 12px; border-left: 4px solid ").append(priorityColor).append("; margin-bottom: 30px;'>");
        content.append("<div style='color: #333333; font-size: 16px; line-height: 1.8; text-align: justify;'>");
        content.append(notification.getMessage().replace("\n", "<br>"));
        content.append("</div>");
        content.append("</div>");
        
        // Attachment section
        if (notification.getAttachmentUrls() != null && !notification.getAttachmentUrls().isEmpty()) {
            content.append("<div style='background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); padding: 20px; border-radius: 12px; margin-bottom: 30px;'>");
            content.append("<h3 style='color: #ffffff; margin: 0 0 15px 0; font-size: 18px; font-weight: 600;'>");
            content.append("📎 File đính kèm (").append(notification.getAttachmentUrls().size()).append(" tệp)");
            content.append("</h3>");
            
            for (int i = 0; i < notification.getAttachmentUrls().size(); i++) {
                String fileName = notification.getAttachmentNames().get(i);
                String fileSize = formatFileSize(notification.getAttachmentSizes().get(i));
                String fileType = notification.getAttachmentTypes().get(i);
                
                // Get file icon based on type
                String fileIcon = getFileIcon(fileType);
                
                content.append("<div style='background: rgba(255,255,255,0.95); margin-bottom: 10px; padding: 15px; border-radius: 8px; display: flex; align-items: center;'>");
                content.append("<div style='background: #007bff; color: white; width: 40px; height: 40px; border-radius: 8px; display: flex; align-items: center; justify-content: center; margin-right: 15px; font-size: 18px;'>");
                content.append(fileIcon);
                content.append("</div>");
                content.append("<div style='flex: 1;'>");
                content.append("<div style='font-weight: 600; color: #333333; margin-bottom: 2px;'>").append(fileName).append("</div>");
                content.append("<div style='color: #6c757d; font-size: 12px;'>Kích thước: ").append(fileSize).append("</div>");
                content.append("</div>");
                content.append("</div>");
            }
            
            content.append("<div style='background: rgba(255,255,255,0.9); padding: 12px; border-radius: 8px; text-align: center;'>");
            content.append("<p style='margin: 0; color: #333333; font-size: 14px;'>");
            content.append("💡 <strong>Lưu ý:</strong> Vui lòng đăng nhập vào hệ thống để tải xuống các tệp đính kèm");
            content.append("</p>");
            content.append("</div>");
            content.append("</div>");
        }
        
        // Action button
        content.append("<div style='text-align: center; margin-bottom: 30px;'>");
        content.append("<a href='http://localhost:8081/teacher/dashboard' style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; padding: 15px 30px; border-radius: 25px; font-size: 16px; font-weight: 600; display: inline-block; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); transition: all 0.3s ease;'>");
        content.append("🚀 Truy cập hệ thống");
        content.append("</a>");
        content.append("</div>");
        
        // Timestamp
        content.append("<div style='text-align: center; padding: 20px; background: #f8f9fa; border-radius: 8px; margin-bottom: 20px;'>");
        content.append("<p style='margin: 0; color: #6c757d; font-size: 14px;'>");
        content.append("🕒 Thời gian gửi: <strong>").append(notification.getCreatedAt().toString().replace("T", " ")).append("</strong>");
        content.append("</p>");
        content.append("</div>");
        
        content.append("</div>"); // End content body
        
        // Footer
        content.append("<div style='background: #333333; padding: 30px 20px; text-align: center;'>");
        content.append("<p style='color: #ffffff; margin: 0 0 10px 0; font-size: 16px; font-weight: 600;'>");
        content.append("E-Learning System");
        content.append("</p>");
        content.append("<p style='color: #cccccc; margin: 0 0 15px 0; font-size: 14px;'>");
        content.append("Hệ thống học tập trực tuyến hiện đại và chuyên nghiệp");
        content.append("</p>");
        content.append("<div style='border-top: 1px solid #555555; padding-top: 20px; margin-top: 20px;'>");
        content.append("<p style='color: #999999; margin: 0; font-size: 12px;'>");
        content.append("© 2024 E-Learning System. Tất cả quyền được bảo lưu.<br>");
        content.append("Email này được gửi tự động, vui lòng không trả lời.");
        content.append("</p>");
        content.append("</div>");
        content.append("</div>");
        
        content.append("</div>"); // End main container
        content.append("</body>");
        content.append("</html>");
        
        return content.toString();
    }
    
    private String getFileIcon(String fileType) {
        if (fileType == null) return "📄";
        
        return switch (fileType.toLowerCase()) {
            case "application/pdf" -> "📕";
            case "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "📘";
            case "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "📊";
            case "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "📈";
            case "text/plain" -> "📝";
            default -> {
                if (fileType.startsWith("image/")) yield "🖼️";
                yield "📄";
            }
        };
    }
    
    private String getPriorityText(String priority) {
        return switch (priority) {
            case "URGENT" -> "Khẩn cấp";
            case "HIGH" -> "Quan trọng";
            case "MEDIUM" -> "Bình thường";
            case "LOW" -> "Thấp";
            default -> "Bình thường";
        };
    }
    
    private String getTypeText(String type) {
        return switch (type) {
            case "GENERAL" -> "Thông báo chung";
            case "COURSE" -> "Khóa học";
            case "ASSIGNMENT" -> "Bài tập";
            case "ANNOUNCEMENT" -> "Thông báo";
            case "REMINDER" -> "Nhắc nhở";
            case "WARNING" -> "Cảnh báo";
            default -> "Thông báo";
        };
    }
    
    private String getPriorityColor(String priority) {
        return switch (priority) {
            case "URGENT" -> "#e74c3c"; // Đỏ đậm cho khẩn cấp
            case "HIGH" -> "#dc3545";   // Đỏ cho quan trọng
            case "MEDIUM" -> "#f39c12"; // Cam cho trung bình
            case "LOW" -> "#28a745";    // Xanh lá cho thấp
            default -> "#6c757d";       // Xám cho mặc định
        };
    }
    
    private String formatFileSize(Long bytes) {
        if (bytes == null) return "0 B";
        
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    // Get notifications for specific teacher (including broadcast)
    public List<Notification> getNotificationsForTeacher(String teacherId) {
        List<Notification> personalNotifications = notificationRepository.findByToUserIdOrderByCreatedAtDesc(teacherId);
        List<Notification> broadcastNotifications = notificationRepository.findByToUserIdIsNullOrderByCreatedAtDesc();
        
        return Stream.concat(personalNotifications.stream(), broadcastNotifications.stream())
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
                .collect(Collectors.toList());
    }
    
    // Get unread notifications count for teacher
    public long getUnreadCountForTeacher(String teacherId) {
        long personalUnread = notificationRepository.countByToUserIdAndIsRead(teacherId, false);
        long broadcastUnread = notificationRepository.countByToUserIdIsNullAndIsRead(false);
        return personalUnread + broadcastUnread;
    }
    
    // Mark notification as read
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
        notificationRepository.save(notification);
        });
    }
    
    // Get all notifications (for admin)
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
    
    // Delete notification
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    public Notification getNotificationById(String notificationId) {
        return notificationRepository.findById(notificationId).orElse(null);
    }

    public Notification updateNotification(String notificationId, String title, String message, String priority, String type) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setPriority(priority);
            notification.setType(type);
            notification.setUpdatedAt(LocalDateTime.now());
            return notificationRepository.save(notification);
        }
        return null;
    }
}
