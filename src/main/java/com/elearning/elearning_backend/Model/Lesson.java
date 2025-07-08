package com.elearning.elearning_backend.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {
    @Id
    private String id;

    private String title;
    @Field("content")
    private List<String> content = new ArrayList<>();
    private String videoUrl;
    private String courseId;
    private String createdByUserId;
    private String bodyPath;
    
    // Thêm trường để lưu nội dung HTML đã được format
    private String contentHtml;
    private String description;
}
