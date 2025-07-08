package com.elearning.elearning_backend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.elearning.elearning_backend.Model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Find main comments (not replies) by course ID and active status
    List<Comment> findByCourseIdAndActiveAndParentIdIsNullOrderByCreatedAtDesc(String courseId, Boolean active);
    
    // Find replies for a specific parent comment
    List<Comment> findByParentIdAndActiveOrderByCreatedAtAsc(Long parentId, Boolean active);
    
    // Find all comments (including replies) by course ID
    List<Comment> findByCourseIdAndActiveOrderByCreatedAtDesc(String courseId, Boolean active);
    
    // Count total comments for a course (excluding replies for rating calculation)
    long countByCourseIdAndActiveAndParentIdIsNull(String courseId, Boolean active);
    
    // Get average rating for a course (only main comments, not replies)
    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.courseId = :courseId AND c.active = :active AND c.parentId IS NULL AND c.rating IS NOT NULL")
    Double getAverageRatingByCourseId(@Param("courseId") String courseId, @Param("active") Boolean active);
    
    // Count comments by rating (only main comments, not replies)
    long countByCourseIdAndActiveAndParentIdIsNullAndRating(String courseId, Boolean active, Integer rating);
    
    // Admin statistics methods
    List<Comment> findByActiveOrderByCreatedAtDesc(Boolean active);
    
    List<Comment> findByActiveAndContentContainingIgnoreCaseOrderByCreatedAtDesc(Boolean active, String keyword);
    
    long countByActive(Boolean active);
} 