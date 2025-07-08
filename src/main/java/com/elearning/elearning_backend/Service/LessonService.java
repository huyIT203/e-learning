package com.elearning.elearning_backend.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import com.elearning.elearning_backend.Model.Lesson;
import com.elearning.elearning_backend.Repository.LessonRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    
    public Lesson createLesson(String courseId, Lesson lesson) {
        try {
            lesson.setCourseId(courseId);
            
            // Ensure content field is initialized if not set
            if (lesson.getContent() == null) {
                lesson.setContent(new ArrayList<>());
            }
            
            return lessonRepository.save(lesson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create lesson: " + e.getMessage(), e);
        }
    }

    public List<Lesson> getLessonsByCourseId(String courseId) {
        return lessonRepository.findByCourseId(courseId);
    }

    public Optional<Lesson> getLessonById(String id) {
        return lessonRepository.findById(id);
    }

    public Lesson updateLesson(String id, Lesson updatedLesson) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        lesson.setTitle(updatedLesson.getTitle());
        
        // Handle both content and contentHtml fields
        if (updatedLesson.getContent() != null) {
            lesson.setContent(updatedLesson.getContent());
        }
        if (updatedLesson.getContentHtml() != null) {
            lesson.setContentHtml(updatedLesson.getContentHtml());
        }
        if (updatedLesson.getDescription() != null) {
            lesson.setDescription(updatedLesson.getDescription());
        }
        if (updatedLesson.getVideoUrl() != null) {
            lesson.setVideoUrl(updatedLesson.getVideoUrl());
        }
        
        return lessonRepository.save(lesson);
    }

    public void deleteLesson(String id) {
        lessonRepository.deleteById(id);
    }

    public String parseMarkdown(String filePath) throws IOException {
        String markdown = Files.readString(Paths.get(filePath));
        return new com.vladsch.flexmark.html.HtmlRenderer.Builder()
                .build()
                .render(new com.vladsch.flexmark.parser.Parser.Builder().build().parse(markdown));
    }

    public String parseDocx(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            XWPFDocument document = new XWPFDocument(fis);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            String text = extractor.getText();
            return "<p>" + text.replace("\n", "<br>") + "</p>";
        }
    }
}
