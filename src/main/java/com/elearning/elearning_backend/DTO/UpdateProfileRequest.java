package com.elearning.elearning_backend.DTO;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String dob;
    private String gender;
    private String phone;
    private String bio;
    private String facebookUrl;
    private String githubUrl;
}
