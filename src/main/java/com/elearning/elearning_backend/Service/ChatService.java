package com.elearning.elearning_backend.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.elearning.elearning_backend.DTO.ChatContactDTO;
import com.elearning.elearning_backend.DTO.ChatMessageDTO;
import com.elearning.elearning_backend.Model.Message;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.MessageRepository;
import com.elearning.elearning_backend.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Transactional
    public ChatMessageDTO sendMessage(ChatMessageDTO messageDTO) {
        // Log the incoming message data
        System.out.println("💬 ChatService.sendMessage called with:");
        System.out.println("SenderId: " + messageDTO.getSenderId());
        System.out.println("ReceiverId: " + messageDTO.getReceiverId());
        System.out.println("Content: " + messageDTO.getContent());
        System.out.println("CourseId: " + messageDTO.getCourseId());
        
        // Validate users exist
        Optional<User> sender = userRepository.findById(messageDTO.getSenderId());
        Optional<User> receiver = userRepository.findById(messageDTO.getReceiverId());
        
        System.out.println("Sender found: " + sender.isPresent());
        System.out.println("Receiver found: " + receiver.isPresent());
        
        if (sender.isEmpty()) {
            System.err.println("❌ Sender not found with ID: " + messageDTO.getSenderId());
            throw new RuntimeException("Sender not found with ID: " + messageDTO.getSenderId());
        }
        
        if (receiver.isEmpty()) {
            System.err.println("❌ Receiver not found with ID: " + messageDTO.getReceiverId());
            throw new RuntimeException("Receiver not found with ID: " + messageDTO.getReceiverId());
        }
        
        // Create and save message
        Message message = new Message();
        message.setSenderId(messageDTO.getSenderId());
        message.setReceiverId(messageDTO.getReceiverId());
        message.setContent(messageDTO.getContent());
        message.setSenderName(sender.get().getName());
        message.setReceiverName(receiver.get().getName());
        message.setSenderRole(sender.get().getRole().name());
        message.setReceiverRole(receiver.get().getRole().name());
        message.setCourseId(messageDTO.getCourseId());
        message.setCourseName(messageDTO.getCourseName());
        message.setIsRead(false);
        message.setType(Message.MessageType.CHAT);
        
        Message savedMessage = messageRepository.save(message);
        System.out.println("💾 Message saved to database with ID: " + savedMessage.getId());
        
        // Convert to DTO
        ChatMessageDTO result = convertToDTO(savedMessage);
        
        // Send real-time message to receiver
        System.out.println("📤 Sending message to receiver: " + messageDTO.getReceiverId());
        messagingTemplate.convertAndSend(
            "/topic/user/" + messageDTO.getReceiverId(),
            result
        );
        
        // Send confirmation to sender  
        System.out.println("📤 Sending confirmation to sender: " + messageDTO.getSenderId());
        messagingTemplate.convertAndSend(
            "/topic/user/" + messageDTO.getSenderId(),
            result
        );
        
        // Broadcast contact update to both sender and receiver
        // This will trigger contact list refresh on both ends
        System.out.println("🔄 Broadcasting contact updates...");
        broadcastContactUpdate(messageDTO.getSenderId());
        broadcastContactUpdate(messageDTO.getReceiverId());
        
        System.out.println("✅ ChatService.sendMessage completed successfully");
        return result;
    }
    
    public List<ChatMessageDTO> getConversation(String userId1, String userId2, String courseId) {
        List<Message> messages = messageRepository.findConversationBetweenUsersInCourse(userId1, userId2, courseId);
        return messages.stream().map(this::convertToDTO).toList();
    }
    
    public List<ChatContactDTO> getChatContacts(String userId) {
        System.out.println("=== DEBUG getChatContacts ===");
        System.out.println("User ID: " + userId);
        
        List<Object[]> contacts = messageRepository.findChatContactsForUser(userId);
        System.out.println("Raw contacts from repository: " + contacts.size() + " items");
        
        // Debug: Print all messages for this user
        List<Message> allMessages = messageRepository.findAllConversationsForUser(userId);
        System.out.println("All messages for user " + userId + ": " + allMessages.size() + " messages");
        for (Message msg : allMessages) {
            System.out.println("  Message: " + msg.getSenderId() + " -> " + msg.getReceiverId() + 
                " | Course: " + msg.getCourseId() + " | Content: " + msg.getContent());
        }
        
        List<ChatContactDTO> result = new ArrayList<>();
        
        for (Object[] contact : contacts) {
            System.out.println("Processing contact: " + Arrays.toString(contact));
            ChatContactDTO dto = new ChatContactDTO();
            String otherUserId = (String) contact[0];
            
            dto.setUserId(otherUserId);
            dto.setUserName((String) contact[1]);
            dto.setUserRole((String) contact[2]);
            dto.setCourseId((String) contact[3]);
            dto.setCourseName((String) contact[4]);
            dto.setLastMessageTime((LocalDateTime) contact[5]);
            
            // Get user's avatar URL
            Optional<User> otherUser = userRepository.findById(otherUserId);
            if (otherUser.isPresent()) {
                dto.setAvatarUrl(otherUser.get().getAvatarUrl());
            }
            
            // Get unread count for messages from this specific contact TO the current user
            Long unreadCount = messageRepository.countUnreadMessagesBetweenUsers(otherUserId, userId);
            dto.setUnreadCount(unreadCount != null ? unreadCount : 0L);
            
            // Get last message content
            Message lastMessage = getLatestMessageBetweenUsers(
                userId, dto.getUserId(), dto.getCourseId()
            );
            if (lastMessage != null) {
                dto.setLastMessage(lastMessage.getContent());
            }
            
            dto.setIsOnline(false); // Will be updated by WebSocket presence
            result.add(dto);
        }
        
        System.out.println("Final result: " + result.size() + " contacts");
        return result;
    }
    
    @Transactional
    public void markMessagesAsRead(String senderId, String receiverId, String courseId) {
        List<Message> unreadMessages = messageRepository.findConversationBetweenUsersInCourse(senderId, receiverId, courseId)
            .stream()
            .filter(m -> m.getReceiverId().equals(receiverId) && !m.getIsRead())
            .toList();
        
        unreadMessages.forEach(message -> message.setIsRead(true));
        messageRepository.saveAll(unreadMessages);
    }
    
    @Transactional
    public void deleteConversation(String userId1, String userId2, String courseId) {
        System.out.println("🗑️ ChatService.deleteConversation called:");
        System.out.println("User1: " + userId1 + ", User2: " + userId2 + ", Course: " + courseId);
        
        // Get all messages between the two users in the specified course
        List<Message> conversationMessages = messageRepository.findConversationBetweenUsersInCourse(userId1, userId2, courseId);
        
        System.out.println("Found " + conversationMessages.size() + " messages to delete");
        
        if (!conversationMessages.isEmpty()) {
            // Delete all messages in the conversation
            messageRepository.deleteAll(conversationMessages);
            System.out.println("✅ Successfully deleted " + conversationMessages.size() + " messages");
            
            // Broadcast contact update to both users to refresh their contact lists
            broadcastContactUpdate(userId1);
            broadcastContactUpdate(userId2);
        } else {
            System.out.println("ℹ️ No messages found to delete");
        }
    }
    
    public Long getUnreadMessageCount(String userId) {
        return messageRepository.countUnreadMessagesForUser(userId);
    }
    
    public List<ChatMessageDTO> getUnreadMessages(String userId) {
        List<Message> messages = messageRepository.findUnreadMessagesForUser(userId);
        return messages.stream().map(this::convertToDTO).toList();
    }
    
    private ChatMessageDTO convertToDTO(Message message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSenderId());
        dto.setReceiverId(message.getReceiverId());
        dto.setContent(message.getContent());
        dto.setSenderName(message.getSenderName());
        dto.setReceiverName(message.getReceiverName());
        dto.setSenderRole(message.getSenderRole());
        dto.setReceiverRole(message.getReceiverRole());
        dto.setCourseId(message.getCourseId());
        dto.setCourseName(message.getCourseName());
        dto.setIsRead(message.getIsRead());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setType(ChatMessageDTO.MessageType.valueOf(message.getType().name()));
        return dto;
    }
    
    // Method to get all users for admin chat
    public List<ChatContactDTO> getAllUsersForAdmin(String adminId) {
        System.out.println("🔍 Getting all users for admin: " + adminId);
        List<User> allUsers = userRepository.findAll();
        List<ChatContactDTO> result = new ArrayList<>();
        
        for (User user : allUsers) {
            // Skip the current admin user
            if (user.getId().equals(adminId)) {
                continue;
            }
            
            ChatContactDTO dto = new ChatContactDTO();
            dto.setUserId(user.getId());
            dto.setUserName(user.getName());
            dto.setUserRole(user.getRole().name());
            dto.setCourseId("GENERAL"); // Use a general course ID for admin chats
            dto.setCourseName("Admin Chat");
            dto.setAvatarUrl(user.getAvatarUrl()); // Set user's avatar URL
            
            // Get last message between admin and this user (check both directions)
            Message lastMessage = getLatestMessageBetweenUsers(
                adminId, user.getId(), "GENERAL"
            );
            
            System.out.println("Last message between admin " + adminId + " and user " + user.getId() + ": " + 
                (lastMessage != null ? lastMessage.getContent() : "null"));
            
            if (lastMessage != null) {
                dto.setLastMessage(lastMessage.getContent());
                dto.setLastMessageTime(lastMessage.getCreatedAt());
            } else {
                dto.setLastMessage("Chưa có tin nhắn");
                // Set a default time to ensure proper sorting (put users with no messages at the end)
                dto.setLastMessageTime(LocalDateTime.of(2000, 1, 1, 0, 0));
            }
            
            // Get unread count for messages sent TO the admin from this user
            Long unreadCount = messageRepository.countUnreadMessagesBetweenUsers(user.getId(), adminId);
            dto.setUnreadCount(unreadCount != null ? unreadCount : 0L);
            
            System.out.println("User " + user.getName() + " - Last message: " + dto.getLastMessage() + 
                ", Unread: " + dto.getUnreadCount());
            
            dto.setIsOnline(false); // Will be updated by WebSocket presence
            result.add(dto);
        }
        
        // Sort by last message time (most recent first), but put users with no messages at the end
        result.sort((a, b) -> {
            // If both have messages, sort by time (newest first)
            if (!a.getLastMessage().equals("Chưa có tin nhắn") && !b.getLastMessage().equals("Chưa có tin nhắn")) {
                return b.getLastMessageTime().compareTo(a.getLastMessageTime());
            }
            // If only one has messages, prioritize the one with messages
            if (!a.getLastMessage().equals("Chưa có tin nhắn")) return -1;
            if (!b.getLastMessage().equals("Chưa có tin nhắn")) return 1;
            // If both have no messages, sort by user name
            return a.getUserName().compareTo(b.getUserName());
        });
        
        System.out.println("📋 Returning " + result.size() + " users for admin chat");
        return result;
    }
    
    // Method to get all users for teacher chat (students in their courses + admin)
    public List<ChatContactDTO> getAllUsersForTeacher(String teacherId) {
        System.out.println("🔍 Getting all users for teacher: " + teacherId);
        
        // Get teacher's courses first
        Optional<User> teacher = userRepository.findById(teacherId);
        if (teacher.isEmpty()) {
            System.err.println("❌ Teacher not found with ID: " + teacherId);
            return new ArrayList<>();
        }
        
        // For MongoDB implementation, we'll get all users and filter them
        // In a real implementation, you might want to use aggregation pipelines
        List<User> allUsers = userRepository.findAll();
        List<User> availableUsers = new ArrayList<>();
        
        // Add all admins
        for (User user : allUsers) {
            if (user.getRole().name().equals("ADMIN")) {
                availableUsers.add(user);
            }
        }
        
        // Add all students (for now, assuming teacher can chat with all students)
        // In a real implementation, you would filter by course enrollment
        for (User user : allUsers) {
            if (user.getRole().name().equals("STUDENT")) {
                availableUsers.add(user);
            }
        }
        
        List<ChatContactDTO> result = new ArrayList<>();
        
        for (User user : availableUsers) {
            // Skip the current teacher user
            if (user.getId().equals(teacherId)) {
                continue;
            }
            
            ChatContactDTO dto = new ChatContactDTO();
            dto.setUserId(user.getId());
            dto.setUserName(user.getName());
            dto.setUserRole(user.getRole().name());
            dto.setAvatarUrl(user.getAvatarUrl());
            
            // Determine course context based on user role
            String courseContext;
            String courseName;
            if (user.getRole().name().equals("ADMIN")) {
                courseContext = "GENERAL";
                courseName = "Admin Chat";
            } else {
                // For students, use a default course context for now
                courseContext = "DEFAULT_COURSE";
                courseName = "Khóa học chung";
            }
            
            dto.setCourseId(courseContext);
            dto.setCourseName(courseName);
            
            // Get last message between teacher and this user
            Message lastMessage = getLatestMessageBetweenUsers(
                teacherId, user.getId(), courseContext
            );
            
            System.out.println("Last message between teacher " + teacherId + " and user " + user.getId() + 
                " in course " + courseContext + ": " + (lastMessage != null ? lastMessage.getContent() : "null"));
            
            if (lastMessage != null) {
                dto.setLastMessage(lastMessage.getContent());
                dto.setLastMessageTime(lastMessage.getCreatedAt());
            } else {
                dto.setLastMessage("Chưa có tin nhắn");
                // Set a default time to ensure proper sorting (put users with no messages at the end)
                dto.setLastMessageTime(LocalDateTime.of(2000, 1, 1, 0, 0));
            }
            
            // Get unread count for messages sent TO the teacher from this user
            Long unreadCount = messageRepository.countUnreadMessagesBetweenUsers(user.getId(), teacherId);
            dto.setUnreadCount(unreadCount != null ? unreadCount : 0L);
            
            System.out.println("User " + user.getName() + " (" + user.getRole() + ") - Course: " + courseName + 
                ", Last message: " + dto.getLastMessage() + ", Unread: " + dto.getUnreadCount());
            
            dto.setIsOnline(false); // Will be updated by WebSocket presence
            result.add(dto);
        }
        
        // Sort by last message time (most recent first), but put users with no messages at the end
        result.sort((a, b) -> {
            // If both have messages, sort by time (newest first)
            if (!a.getLastMessage().equals("Chưa có tin nhắn") && !b.getLastMessage().equals("Chưa có tin nhắn")) {
                return b.getLastMessageTime().compareTo(a.getLastMessageTime());
            }
            // If only one has messages, prioritize the one with messages
            if (!a.getLastMessage().equals("Chưa có tin nhắn")) return -1;
            if (!b.getLastMessage().equals("Chưa có tin nhắn")) return 1;
            // If both have no messages, sort by user name
            return a.getUserName().compareTo(b.getUserName());
        });
        
        System.out.println("📋 Returning " + result.size() + " users for teacher chat");
        return result;
    }
    
    // Simplified helper methods for MongoDB
    private List<User> findStudentsInTeacherCoursesAndAdmins(String teacherId) {
        List<User> allUsers = userRepository.findAll();
        List<User> result = new ArrayList<>();
        
        // Add all admins and students
        for (User user : allUsers) {
            if (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("STUDENT")) {
                result.add(user);
            }
        }
        
        return result;
    }
    
    private List<String> findSharedCourses(String teacherId, String studentId) {
        // For now, return a default course
        // In a real implementation, you would query the course/enrollment collections
        List<String> sharedCourses = new ArrayList<>();
        sharedCourses.add("DEFAULT_COURSE");
        return sharedCourses;
    }
    
    // Remove the old helper methods that were causing errors
    // Helper method to find shared course between teacher and student
    private String findSharedCourse(String teacherId, String studentId) {
        // This would typically query the enrollment/course tables
        // For now, return a default course ID - you may need to implement this based on your course structure
        List<String> sharedCourses = findSharedCourses(teacherId, studentId);
        return sharedCourses.isEmpty() ? "DEFAULT_COURSE" : sharedCourses.get(0);
    }
    
    // New method to broadcast contact updates
    private void broadcastContactUpdate(String userId) {
        try {
            System.out.println("Broadcasting contact update to user: " + userId);
            // Send a simple notification to refresh contacts
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/contact-updates",
                "{\"type\": \"refresh\", \"timestamp\": \"" + System.currentTimeMillis() + "\"}"
            );
        } catch (Exception e) {
            System.err.println("Error broadcasting contact update: " + e.getMessage());
        }
    }

    // Helper method to get latest message between users
    private Message getLatestMessageBetweenUsers(String userId1, String userId2, String courseId) {
        Pageable pageable = PageRequest.of(0, 1); // Get first page with 1 result
        List<Message> messages = messageRepository.findLatestMessageBetweenUsersInCourseWithPageable(
            userId1, userId2, courseId, pageable
        );
        return messages.isEmpty() ? null : messages.get(0);
    }
} 