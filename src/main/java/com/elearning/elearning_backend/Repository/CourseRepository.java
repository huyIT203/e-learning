package com.elearning.elearning_backend.Repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.elearning.elearning_backend.Enum.CourseStatus;
import com.elearning.elearning_backend.Model.Course;

public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findByCreatedByUserId(String userId);
    int countByEnrolledUserIdsContaining(String userId);
    List<Course> findByEnrolledUserIdsContaining(String userId);
    List<Course> findByStatus(CourseStatus status);
    List<Course> findByTeacherId(String teacherId);
    Optional<Course> findById(ObjectId id);
    
    // Category-based search methods
    List<Course> findByJobRole(String jobRole);
    List<Course> findBySkillCategory(String skillCategory);
    List<Course> findByJobRoleAndSkillCategory(String jobRole, String skillCategory);
    
    // Count methods for categories
    long countByJobRole(String jobRole);
    long countBySkillCategory(String skillCategory);
}
