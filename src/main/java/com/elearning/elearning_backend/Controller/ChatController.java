package com.elearning.elearning_backend.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.elearning.elearning_backend.DTO.ChatContactDTO;
import com.elearning.elearning_backend.DTO.ChatMessageDTO;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.MessageRepository;
import com.elearning.elearning_backend.Repository.UserRepository;
import com.elearning.elearning_backend.Service.ChatService;
import com.elearning.elearning_backend.Service.CourseService;
import com.elearning.elearning_backend.Service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final UserService userService;
    private final MessageRepository messageRepository;
    
    // WebSocket endpoints
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage) {
        System.out.println("🚀 ChatController.sendMessage received: " + chatMessage.getContent());
        System.out.println("From: " + chatMessage.getSenderId() + " To: " + chatMessage.getReceiverId());
        
        // Save message and let ChatService handle the WebSocket broadcasting
        ChatMessageDTO savedMessage = chatService.sendMessage(chatMessage);
        System.out.println("✅ Message saved and broadcasted via ChatService");
    }
    
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("👤 User joined chat: " + chatMessage.getSenderName() + " (ID: " + chatMessage.getSenderId() + ")");
        
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("userId", chatMessage.getSenderId());
        headerAccessor.getSessionAttributes().put("userName", chatMessage.getSenderName());
        
        // Notify others that user joined (optional - you can remove this if not needed)
        chatMessage.setType(ChatMessageDTO.MessageType.JOIN);
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }
    
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload ChatMessageDTO chatMessage) {
        System.out.println("⌨️ Typing notification: " + chatMessage.getSenderName() + " is typing to " + chatMessage.getReceiverId());
        chatMessage.setType(ChatMessageDTO.MessageType.TYPING);
        messagingTemplate.convertAndSendToUser(
            chatMessage.getReceiverId(),
            "/queue/typing",
            chatMessage
        );
    }
    
    @MessageMapping("/chat.stopTyping")
    public void handleStopTyping(@Payload ChatMessageDTO chatMessage) {
        chatMessage.setType(ChatMessageDTO.MessageType.STOP_TYPING);
        messagingTemplate.convertAndSendToUser(
            chatMessage.getReceiverId(),
            "/queue/typing",
            chatMessage
        );
    }
    
    // REST API endpoints
    @GetMapping("/api/chat/contacts")
    @ResponseBody
    public ResponseEntity<List<ChatContactDTO>> getChatContacts(Principal principal) {
        System.out.println("=== DEBUG ChatController.getChatContacts ===");
        System.out.println("Principal name (email): " + principal.getName());
        
        String userEmail = principal.getName();
        
        // Find user by email to get user ID
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            System.err.println("User not found with email: " + userEmail);
            return ResponseEntity.badRequest().build();
        }
        
        User user = userOptional.get();
        String userId = user.getId();
        System.out.println("Found user ID: " + userId + " for email: " + userEmail);
        
        List<ChatContactDTO> contacts = chatService.getChatContacts(userId);
        System.out.println("Returning " + contacts.size() + " contacts");
        
        return ResponseEntity.ok(contacts);
    }
    
    @GetMapping("/api/chat/admin/all-users")
    @ResponseBody
    public ResponseEntity<List<ChatContactDTO>> getAllUsersForAdmin(Principal principal) {
        System.out.println("=== DEBUG ChatController.getAllUsersForAdmin ===");
        System.out.println("Principal name (email): " + principal.getName());
        
        String adminEmail = principal.getName();
        
        // Find admin user by email to get user ID
        Optional<User> adminOptional = userRepository.findByEmail(adminEmail);
        if (adminOptional.isEmpty()) {
            System.err.println("Admin user not found with email: " + adminEmail);
            return ResponseEntity.badRequest().build();
        }
        
        User admin = adminOptional.get();
        String adminId = admin.getId();
        System.out.println("Found admin ID: " + adminId + " for email: " + adminEmail);
        
        List<ChatContactDTO> users = chatService.getAllUsersForAdmin(adminId);
        System.out.println("Returning " + users.size() + " users for admin");
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/api/chat/teacher/all-users")
    @ResponseBody
    public ResponseEntity<List<ChatContactDTO>> getAllUsersForTeacher(Principal principal) {
        System.out.println("=== DEBUG ChatController.getAllUsersForTeacher ===");
        System.out.println("Principal name (email): " + principal.getName());
        
        String teacherEmail = principal.getName();
        
        // Find teacher user by email to get user ID
        Optional<User> teacherOptional = userRepository.findByEmail(teacherEmail);
        if (teacherOptional.isEmpty()) {
            System.err.println("Teacher user not found with email: " + teacherEmail);
            return ResponseEntity.badRequest().build();
        }
        
        User teacher = teacherOptional.get();
        String teacherId = teacher.getId();
        System.out.println("Found teacher ID: " + teacherId + " for email: " + teacherEmail);
        
        List<ChatContactDTO> users = chatService.getAllUsersForTeacher(teacherId);
        System.out.println("Returning " + users.size() + " users for teacher");
        
        return ResponseEntity.ok(users);
    }
    
    @PostMapping("/api/chat/markAsRead")
    @ResponseBody
    public ResponseEntity<Void> markAsRead(
            @RequestParam String senderId,
            @RequestParam String receiverId,
            @RequestParam String courseId) {
        chatService.markMessagesAsRead(senderId, receiverId, courseId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/api/chat/conversation")
    @ResponseBody
    public ResponseEntity<Void> deleteConversation(
            @RequestParam String userId1,
            @RequestParam String userId2,
            @RequestParam String courseId,
            Principal principal) {
        System.out.println("🗑️ Delete conversation request:");
        System.out.println("User1: " + userId1 + ", User2: " + userId2 + ", Course: " + courseId);
        System.out.println("Requested by: " + principal.getName());
        
        try {
            chatService.deleteConversation(userId1, userId2, courseId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("❌ Error deleting conversation: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/api/chat/unread-count")
    @ResponseBody
    public ResponseEntity<Long> getUnreadCount(Principal principal) {
        String userId = principal.getName();
        Long count = chatService.getUnreadMessageCount(userId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/api/chat/unread-messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDTO>> getUnreadMessages(Principal principal) {
        String userId = principal.getName();
        List<ChatMessageDTO> messages = chatService.getUnreadMessages(userId);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/api/chat/conversation")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDTO>> getConversation(
            @RequestParam String userId1,
            @RequestParam String userId2,
            @RequestParam String courseId,
            Principal principal) {
        System.out.println("=== DEBUG ChatController.getConversation ===");
        System.out.println("User1: " + userId1 + ", User2: " + userId2 + ", Course: " + courseId);
        System.out.println("Requested by: " + principal.getName());
        
        try {
            List<ChatMessageDTO> messages = chatService.getConversation(userId1, userId2, courseId);
            System.out.println("Returning " + messages.size() + " messages");
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("❌ Error loading conversation: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 