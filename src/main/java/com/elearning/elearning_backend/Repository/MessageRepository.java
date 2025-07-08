package com.elearning.elearning_backend.Repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.elearning.elearning_backend.Model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Lấy tin nhắn giữa 2 người trong 1 khóa học
    @Query("SELECT m FROM Message m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1)) AND " +
           "m.courseId = :courseId " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversationBetweenUsersInCourse(
        @Param("userId1") String userId1, 
        @Param("userId2") String userId2, 
        @Param("courseId") String courseId
    );
    
    // Lấy tất cả cuộc trò chuyện của 1 user
    @Query("SELECT DISTINCT m FROM Message m WHERE " +
           "(m.senderId = :userId OR m.receiverId = :userId) " +
           "ORDER BY m.createdAt DESC")
    List<Message> findAllConversationsForUser(@Param("userId") String userId);
    
    // Lấy tin nhắn chưa đọc
    @Query("SELECT m FROM Message m WHERE m.receiverId = :userId AND m.isRead = false " +
           "ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessagesForUser(@Param("userId") String userId);
    
    // Đếm tin nhắn chưa đọc
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :userId AND m.isRead = false")
    Long countUnreadMessagesForUser(@Param("userId") String userId);
    
    // Lấy tin nhắn mới nhất giữa 2 người trong 1 khóa học - sử dụng Pageable
    @Query("SELECT m FROM Message m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1)) AND " +
           "m.courseId = :courseId " +
           "ORDER BY m.createdAt DESC")
    List<Message> findLatestMessageBetweenUsersInCourseWithPageable(
        @Param("userId1") String userId1, 
        @Param("userId2") String userId2, 
        @Param("courseId") String courseId,
        Pageable pageable
    );
    
    // Lấy danh sách người đã chat với user trong các khóa học
    @Query("SELECT DISTINCT " +
           "CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END as otherUserId, " +
           "CASE WHEN m.senderId = :userId THEN m.receiverName ELSE m.senderName END as otherUserName, " +
           "CASE WHEN m.senderId = :userId THEN m.receiverRole ELSE m.senderRole END as otherUserRole, " +
           "m.courseId, m.courseName, MAX(m.createdAt) as lastMessageTime " +
           "FROM Message m WHERE (m.senderId = :userId OR m.receiverId = :userId) " +
           "GROUP BY otherUserId, otherUserName, otherUserRole, m.courseId, m.courseName " +
           "ORDER BY lastMessageTime DESC")
    List<Object[]> findChatContactsForUser(@Param("userId") String userId);
    
    // Đếm tin nhắn chưa đọc giữa 2 người
    @Query("SELECT COUNT(m) FROM Message m WHERE m.senderId = :senderId AND m.receiverId = :receiverId AND m.isRead = false")
    Long countUnreadMessagesBetweenUsers(@Param("senderId") String senderId, @Param("receiverId") String receiverId);
} 