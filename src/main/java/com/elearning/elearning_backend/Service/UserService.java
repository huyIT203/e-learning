package com.elearning.elearning_backend.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.elearning.elearning_backend.DTO.UpdateProfileRequest;
import com.elearning.elearning_backend.Enum.Role;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void updateAvatar(String email, String avatarUrl) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    public void updateProfile(String email,  UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        user.setName(request.getName());
        user.setDob(request.getDob());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        user.setBio(request.getBio());
        user.setFacebookUrl(request.getFacebookUrl());
        user.setGithubUrl(request.getGithubUrl());

        userRepository.save(user);
    }

    // Methods for ChatController
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> findAllTeachers() {
        return userRepository.findByRole(Role.TEACHER);
    }

    public List<User> findAllAdmins() {
        return userRepository.findByRole(Role.ADMIN);
    }

    public List<User> findAllExceptCurrent(String currentUserId) {
        return userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .collect(Collectors.toList());
    }
}
