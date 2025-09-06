package com.elearning.elearning_backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.elearning.elearning_backend.Model.Category;
import com.elearning.elearning_backend.Model.Category.CategoryType;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByTypeAndActiveTrue(CategoryType type);
    List<Category> findByActiveTrue();
    Optional<Category> findByNameAndActiveTrue(String name);
    List<Category> findByDisplayNameContainingIgnoreCaseAndActiveTrue(String displayName);
    boolean existsByNameAndActiveTrue(String name);
    List<Category> findByTypeAndDisplayNameContainingIgnoreCaseAndActiveTrue(CategoryType type, String displayName);
} 