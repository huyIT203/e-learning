package com.elearning.elearning_backend.Model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {
    @Id
    private String id;
    
    private String title; // Tiêu đề đề thi
    private String description; // Mô tả đề thi
    private String category; // Danh mục (Toán, Văn, Anh, v.v.)
    private String tags; // Tags để tìm kiếm
    
    // Cấu hình đề thi
    private ExamType type; // Loại đề thi
    private DifficultyLevel difficulty; // Độ khó tổng thể
    private int duration; // Thời gian làm bài (phút)
    private int totalQuestions; // Tổng số câu hỏi
    private int totalPoints; // Tổng điểm
    
    // Danh sách câu hỏi
    private List<String> questionIds; // IDs của các câu hỏi
    private List<ExamQuestion> questions; // Chi tiết câu hỏi trong đề
    
    // Cấu hình hiển thị
    private boolean shuffleQuestions; // Xáo trộn câu hỏi
    private boolean shuffleOptions; // Xáo trộn lựa chọn
    private boolean showResults; // Hiển thị kết quả ngay
    private boolean allowReview; // Cho phép xem lại
    private boolean allowRetry; // Cho phép làm lại
    private int maxAttempts; // Số lần làm tối đa
    
    // Điều kiện truy cập
    private String password; // Mật khẩu (nếu có)
    private LocalDateTime startTime; // Thời gian bắt đầu
    private LocalDateTime endTime; // Thời gian kết thúc
    private List<String> allowedUsers; // Danh sách user được phép làm
    
    // Người tạo
    private String createdBy; // User ID
    private String createdByName; // Tên người tạo
    
    // Trạng thái
    private ExamStatus status;
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> settings; // Cài đặt bổ sung
    
    // Statistics
    private int totalAttempts; // Tổng lượt làm bài
    private double averageScore; // Điểm trung bình
    private int passCount; // Số người đậu
    private int failCount; // Số người trượt
    
    // Enums
    public enum ExamType {
        PRACTICE("Luyện tập"),
        QUIZ("Kiểm tra nhanh"),
        MIDTERM("Kiểm tra giữa kỳ"),
        FINAL("Thi cuối kỳ"),
        ENTRANCE("Thi đầu vào"),
        CERTIFICATION("Chứng chỉ"),
        CUSTOM("Tùy chỉnh");
        
        private final String displayName;
        
        ExamType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum DifficultyLevel {
        EASY("Dễ"),
        MEDIUM("Trung bình"),
        HARD("Khó"),
        MIXED("Hỗn hợp");
        
        private final String displayName;
        
        DifficultyLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ExamStatus {
        DRAFT("Bản nháp"),
        PUBLISHED("Đã xuất bản"),
        ACTIVE("Đang hoạt động"),
        ENDED("Đã kết thúc"),
        ARCHIVED("Đã lưu trữ"),
        DELETED("Đã xóa");
        
        private final String displayName;
        
        ExamStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Inner class for exam questions
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExamQuestion {
        private String questionId;
        private int order; // Thứ tự trong đề thi
        private int points; // Điểm cho câu hỏi này
        private boolean required; // Bắt buộc trả lời
        private Map<String, Object> config; // Cấu hình riêng cho câu hỏi
    }
}
