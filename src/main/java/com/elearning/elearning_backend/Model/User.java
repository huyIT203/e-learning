package com.elearning.elearning_backend.Model;

import java.time.LocalDateTime;

    import jakarta.persistence.Entity;
    import org.springframework.data.annotation.Id;
    import org.springframework.data.mongodb.core.mapping.Document;

    import com.elearning.elearning_backend.Enum.Role;

    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
        @Id
        private String id;
        private String name;
        private String email;
        private String password;
        private String avatarUrl;
        private Role role;
        private String status; // active, inactive, suspended
        private LocalDateTime lastActivityAt;
        private LocalDateTime createdAt;

        private String dob;
        private String gender;
        private String phone;
        private String bio;
        private String facebookUrl;
        private String githubUrl;

}
