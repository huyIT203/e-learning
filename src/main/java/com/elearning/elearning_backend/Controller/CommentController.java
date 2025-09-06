package com.elearning.elearning_backend.Controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elearning.elearning_backend.Model.Comment;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.UserRepository;
import com.elearning.elearning_backend.Service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCommentsByCourse(@PathVariable String courseId) {
        try {
            return ResponseEntity.ok(commentService.getCommentsByCourseId(courseId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/course/{courseId}/stats")
    public ResponseEntity<?> getCourseRatingStats(@PathVariable String courseId) {
        try {
            return ResponseEntity.ok(commentService.getCourseRatingStats(courseId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody Map<String, Object> commentData) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            Comment comment = new Comment();
            comment.setCourseId((String) commentData.get("courseId"));
            comment.setUserId(currentUser.getId());
            comment.setUserName(currentUser.getName());
            comment.setUserRole(currentUser.getRole().name()); // Add user role
            comment.setContent((String) commentData.get("content"));
            
            // Set rating if provided (for main comments)
            if (commentData.get("rating") != null) {
                comment.setRating((Integer) commentData.get("rating"));
            }
            
            Comment savedComment = commentService.addComment(comment);
            
            // 🔥 REAL-TIME: Broadcast new comment to all users viewing this course
            System.out.println("📤 Broadcasting new comment to course: " + savedComment.getCourseId());
            messagingTemplate.convertAndSend(
                "/topic/course/" + savedComment.getCourseId() + "/comments",
                Map.of(
                    "type", "NEW_COMMENT",
                    "comment", savedComment,
                    "action", "reload_comments"
                )
            );
            
            // Also broadcast rating stats update
            Map<String, Object> ratingStats = commentService.getCourseRatingStats(savedComment.getCourseId());
            messagingTemplate.convertAndSend(
                "/topic/course/" + savedComment.getCourseId() + "/rating-stats",
                Map.of(
                    "type", "RATING_STATS_UPDATE",
                    "stats", ratingStats,
                    "action", "update_rating_stats"
                )
            );
            
            return ResponseEntity.ok(savedComment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{commentId}/reply")
    public ResponseEntity<?> addReply(@PathVariable Long commentId, @RequestBody Map<String, Object> replyData) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            // Temporarily allow all logged-in users to reply for testing
            // TODO: Later restrict to TEACHER and ADMIN only
            /*
            if (!currentUser.getRole().name().equals("TEACHER") && !currentUser.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(403).body(Map.of("error", "Only teachers and admins can reply to comments"));
            }
            */
            
            Comment reply = new Comment();
            reply.setUserId(currentUser.getId());
            reply.setUserName(currentUser.getName());
            reply.setUserRole(currentUser.getRole().name()); // Add user role
            reply.setContent((String) replyData.get("content"));
            
            Comment savedReply = commentService.addReply(commentId, reply);
            
            // 🔥 REAL-TIME: Broadcast new reply to all users viewing this course
            System.out.println("📤 Broadcasting new reply to course: " + savedReply.getCourseId());
            messagingTemplate.convertAndSend(
                "/topic/course/" + savedReply.getCourseId() + "/comments",
                Map.of(
                    "type", "NEW_REPLY",
                    "reply", savedReply,
                    "parentId", commentId,
                    "action", "reload_comments"
                )
            );
            
            return ResponseEntity.ok(savedReply);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            // Only allow TEACHER and ADMIN to delete comments
            if (!currentUser.getRole().name().equals("TEACHER") && !currentUser.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(403).body(Map.of("error", "Only teachers and admins can delete comments"));
            }
            
            // Get comment before deletion to get courseId
            Optional<Comment> commentOpt = commentService.getCommentById(commentId);
            if (commentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Comment not found"));
            }
            
            String courseId = commentOpt.get().getCourseId();
            
            commentService.deleteComment(commentId.toString());
            
            // 🔥 REAL-TIME: Broadcast comment deletion to all users viewing this course
            System.out.println("📤 Broadcasting comment deletion to course: " + courseId);
            messagingTemplate.convertAndSend(
                "/topic/course/" + courseId + "/comments",
                Map.of(
                    "type", "COMMENT_DELETED",
                    "commentId", commentId,
                    "action", "reload_comments"
                )
            );
            
            // Also broadcast rating stats update
            Map<String, Object> ratingStats = commentService.getCourseRatingStats(courseId);
            messagingTemplate.convertAndSend(
                "/topic/course/" + courseId + "/rating-stats",
                Map.of(
                    "type", "RATING_STATS_UPDATE",
                    "stats", ratingStats,
                    "action", "update_rating_stats"
                )
            );
            
            return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // 🔥 WEBSOCKET: Add WebSocket endpoint for real-time comment operations
    @MessageMapping("/comments.addComment")
    public void addCommentViaWebSocket(@Payload Map<String, Object> commentData) {
        try {
            System.out.println("🚀 WebSocket addComment received: " + commentData);
            
            // Process the comment addition (similar to REST endpoint)
            // This allows for even more real-time interaction if needed
            String courseId = (String) commentData.get("courseId");
            
            // Broadcast to all users in the course
            messagingTemplate.convertAndSend(
                "/topic/course/" + courseId + "/comments",
                Map.of(
                    "type", "COMMENT_UPDATE",
                    "action", "reload_comments"
                )
            );
            
        } catch (Exception e) {
            System.err.println("Error in WebSocket addComment: " + e.getMessage());
        }
    }
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            return userOpt.orElse(null);
        }
        return null;
    }
} 
