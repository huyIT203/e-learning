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

@Document(collection = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    @Id
    private String id;
    
    private String title; // Tiêu đề câu hỏi
    private String content; // Nội dung câu hỏi
    private QuestionType type; // Loại câu hỏi
    private String category; // Danh mục (Toán, Văn, Anh, v.v.)
    private String tags; // Tags để tìm kiếm
    private DifficultyLevel difficulty; // Độ khó
    
    // Các lựa chọn cho câu hỏi trắc nghiệm
    private List<Option> options;
    
    // Đáp án đúng
    private String correctAnswer;
    
    // Giải thích đáp án
    private String explanation;
    
    // Điểm số
    private int points;
    
    // Thời gian làm bài (giây)
    private int timeLimit;
    
    // Người tạo
    private String createdBy; // User ID
    private String createdByName; // Tên người tạo
    
    // Trạng thái
    private QuestionStatus status;
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> additionalData; // Dữ liệu bổ sung
    
    // Enums
    public enum QuestionType {
        MULTIPLE_CHOICE("Trắc nghiệm (chọn một)"),
        MULTIPLE_SELECT("Trắc nghiệm (chọn nhiều)"),
        TRUE_FALSE("Đúng/Sai"),
        ESSAY("Tự luận"),
        FILL_BLANK("Điền vào chỗ trống"),
        MATCHING("Nối đáp án"),
        ORDERING("Sắp xếp"),
        DRAG_DROP("Kéo thả"),
        HOTSPOT("Chọn điểm trên hình"),
        MATH("Toán học");
        
        private final String displayName;
        
        QuestionType(String displayName) {
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
        EXPERT("Chuyên gia");
        
        private final String displayName;
        
        DifficultyLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum QuestionStatus {
        DRAFT("Bản nháp"),
        PUBLISHED("Đã xuất bản"),
        ARCHIVED("Đã lưu trữ"),
        DELETED("Đã xóa");
        
        private final String displayName;
        
        QuestionStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Inner class for options
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Option {
        private String id;
        private String text;
        private boolean isCorrect;
        private String explanation; // Giải thích cho lựa chọn này
        private String imageUrl; // URL hình ảnh nếu có
    }
}
