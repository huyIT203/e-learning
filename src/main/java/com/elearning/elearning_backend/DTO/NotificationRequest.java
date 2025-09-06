package com.elearning.elearning_backend.DTO;

import lombok.Data;

@Data
public class NotificationRequest {
    private String email;
    private String title;
    private String content;
}
