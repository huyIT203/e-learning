package com.elearning.elearning_backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.elearning.elearning_backend.Enum.Role;
import com.elearning.elearning_backend.Model.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    boolean existsByEmail(String email);
    long countByRole(Role role);
    
    // For now, we'll implement these methods in the service layer
    // since MongoDB queries for complex joins are different from JPA
    // These methods will be implemented in ChatService as helper methods
}
