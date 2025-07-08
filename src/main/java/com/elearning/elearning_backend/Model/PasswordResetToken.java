package com.elearning.elearning_backend.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "password_reset_tokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetToken {
    @Id
    private String id;

    private String email;
    private String token;
    private Date expiryDate;

}
