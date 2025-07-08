package com.elearning.elearning_backend.DTO;

import com.elearning.elearning_backend.Enum.Role;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String  email;
    private String password;
    private Role role;


}
