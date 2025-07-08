package com.elearning.elearning_backend.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private String id;
    private String name;
    private List<String> participants; // List of user IDs
    private String lastMessage;
    private String lastMessageSender;
    private Long lastMessageTime;
    private Integer unreadCount;
    private String roomType; // PRIVATE, GROUP
    private Long createdAt;
    private String createdBy;
    
    // Constructor cho phòng chat private
    public ChatRoomDTO(String user1Id, String user2Id, String user1Name, String user2Name) {
        this.participants = List.of(user1Id, user2Id);
        this.name = user1Name + " & " + user2Name;
        this.roomType = "PRIVATE";
        this.createdAt = System.currentTimeMillis();
        this.unreadCount = 0;
    }
} 