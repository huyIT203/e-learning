package com.elearning.elearning_backend.DTO;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String oldPassword;
    private String newPassword;
}
