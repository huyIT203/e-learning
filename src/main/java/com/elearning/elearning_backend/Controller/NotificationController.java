package com.elearning.elearning_backend.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.elearning.elearning_backend.DTO.NotificationRequest;
import com.elearning.elearning_backend.Model.Notification;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.UserRepository;
import com.elearning.elearning_backend.Service.NotificationService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@RequestParam String email) {
        // Find user by email and get their notifications
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            String userId = userOpt.get().getId();
            return ResponseEntity.ok(notificationService.getNotificationsForTeacher(userId));
        }
        return ResponseEntity.ok(List.of());
    }

    @PostMapping
    public ResponseEntity<Notification> sendNotification(@RequestBody NotificationRequest request) {
        // Create a new notification using the service
        Notification notification = notificationService.createNotification(
            request.getTitle(), 
            request.getContent(), 
            "SYSTEM", // fromUserId - could be admin or system
            null, // toUserId - null for broadcast
            "NORMAL", // priority
            "GENERAL" // type
        );
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
