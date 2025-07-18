package com.elearning.elearning_backend.Repository;

import com.elearning.elearning_backend.Model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken,String> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByEmail(String email);
}
