package com.elearning.elearning_backend.Controller;

import com.elearning.elearning_backend.DTO.UpdateProfileRequest;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserRepository userRepository;
    @GetMapping
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<User> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestBody UpdateProfileRequest request) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(user -> {
                    user.setName(request.getName());
                    user.setDob(request.getDob());
                    user.setGender(request.getGender());
                    user.setPhone(request.getPhone());
                    user.setBio(request.getBio());
                    user.setFacebookUrl(request.getFacebookUrl());
                    user.setGithubUrl(request.getGithubUrl());
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
