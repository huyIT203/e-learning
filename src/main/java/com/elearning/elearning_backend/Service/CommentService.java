package com.elearning.elearning_backend.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.elearning.elearning_backend.Model.Comment;
import com.elearning.elearning_backend.Repository.CommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    
    public List<Comment> getCommentsByCourseId(String courseId) {
        // Get main comments (not replies)
        List<Comment> mainComments = commentRepository.findByCourseIdAndActiveAndParentIdIsNullOrderByCreatedAtDesc(courseId, true);
        
        // Load replies for each main comment
        for (Comment comment : mainComments) {
            List<Comment> replies = commentRepository.findByParentIdAndActiveOrderByCreatedAtAsc(comment.getId(), true);
            comment.setReplies(replies);
        }
        
        return mainComments;
    }
    
    public Comment addComment(Comment comment) {
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setActive(true);
        return commentRepository.save(comment);
    }
    
    public Comment addReply(Long parentId, Comment reply) {
        // Verify parent comment exists
        Optional<Comment> parentComment = commentRepository.findById(parentId);
        if (parentComment.isEmpty()) {
            throw new RuntimeException("Parent comment not found");
        }
        
        reply.setParentId(parentId);
        reply.setCourseId(parentComment.get().getCourseId()); // Set course ID from parent
        reply.setRating(null); // Replies don't have ratings
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());
        reply.setActive(true);
        
        return commentRepository.save(reply);
    }
    
    public Optional<Comment> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }
    
    public Map<String, Object> getCourseRatingStats(String courseId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Get main comments only (not replies) for rating calculation
        List<Comment> mainComments = commentRepository.findByCourseIdAndActiveAndParentIdIsNullOrderByCreatedAtDesc(courseId, true);
        
        // Filter comments that have ratings
        List<Comment> ratedComments = mainComments.stream()
                .filter(comment -> comment.getRating() != null && comment.getRating() > 0)
                .collect(Collectors.toList());
        
        // Calculate average rating
        double averageRating = ratedComments.stream()
                .mapToInt(Comment::getRating)
                .average()
                .orElse(0.0);
        
        // Get total comments count (main comments only for rating stats)
        long totalComments = mainComments.size();
        
        // Get rating breakdown (distribution)
        Map<Integer, Long> ratingBreakdown = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            final int currentRating = rating; // Make variable effectively final for lambda
            long count = ratedComments.stream()
                    .filter(comment -> comment.getRating() == currentRating)
                    .count();
            ratingBreakdown.put(rating, count);
        }
        
        stats.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        stats.put("totalComments", totalComments);
        stats.put("ratingBreakdown", ratingBreakdown);
        
        return stats;
    }
    
    public void deleteComment(String commentId) {
        Optional<Comment> commentOpt = commentRepository.findById(Long.parseLong(commentId));
        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();
            comment.setActive(false);
            comment.setUpdatedAt(LocalDateTime.now());
            commentRepository.save(comment);
            
            // Also deactivate all replies
            List<Comment> replies = commentRepository.findByParentIdAndActiveOrderByCreatedAtAsc(comment.getId(), true);
            for (Comment reply : replies) {
                reply.setActive(false);
                reply.setUpdatedAt(LocalDateTime.now());
                commentRepository.save(reply);
            }
        }
    }
    
    // Admin statistics methods
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }
    
    public List<Comment> getActiveComments() {
        return commentRepository.findByActiveOrderByCreatedAtDesc(true);
    }
    
    public List<Comment> searchComments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getActiveComments();
        }
        return commentRepository.findByActiveAndContentContainingIgnoreCaseOrderByCreatedAtDesc(true, keyword.trim());
    }
    
    public long getTotalCommentsCount() {
        return commentRepository.countByActive(true);
    }
    
    public Map<String, Object> getCommentStatsByCourse(String courseId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Comment> courseComments = commentRepository.findByCourseIdAndActiveOrderByCreatedAtDesc(courseId, true);
        stats.put("totalComments", courseComments.size());
        
        // Get rating stats
        Map<String, Object> ratingStats = getCourseRatingStats(courseId);
        stats.putAll(ratingStats);
        
        return stats;
    }
    
    public List<Map<String, Object>> getAllCourseCommentStats() {
        List<Map<String, Object>> allStats = new ArrayList<>();
        
        // Get all unique course IDs from comments
        List<String> courseIds = commentRepository.findAll().stream()
                .map(Comment::getCourseId)
                .distinct()
                .collect(Collectors.toList());
        
        for (String courseId : courseIds) {
            Map<String, Object> courseStats = getCommentStatsByCourse(courseId);
            courseStats.put("courseId", courseId);
            allStats.add(courseStats);
        }
        
        return allStats;
    }
} 