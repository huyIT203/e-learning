package com.elearning.elearning_backend.Controller;

import com.elearning.elearning_backend.DTO.UpdateProfileRequest;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.UserRepository;
import com.elearning.elearning_backend.Enum.Role;
import com.elearning.elearning_backend.Service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserRole(@PathVariable String id, @RequestBody RoleUpdateRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        user.setRole(req.getNewRole());
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserStatus(@PathVariable String id, @RequestBody StatusUpdateRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        user.setStatus(req.getNewStatus());
        // You can also save the reason to a separate table or log if needed
        return ResponseEntity.ok(userRepository.save(user));
    }

    @Data
    public static class RoleUpdateRequest {
        private Role newRole;
    }
    
    @Data
    public static class StatusUpdateRequest {
        private String newStatus;
        private String reason;
    }

    @PutMapping("/profile/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updateAvatar(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestParam String avatarUrl) {
        userService.updateAvatar(userDetails.getUsername(), avatarUrl);
        return ResponseEntity.ok("Cập nhật avatar thành công.");
    }
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<String> updateProfile(@RequestBody UpdateProfileRequest request,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        userService.updateProfile(email, request);
        return ResponseEntity.ok("Cập nhật hồ sơ thành công.");
    }

}
