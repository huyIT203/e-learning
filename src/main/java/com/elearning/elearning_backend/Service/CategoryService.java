package com.elearning.elearning_backend.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.elearning.elearning_backend.Model.Category;
import com.elearning.elearning_backend.Model.Category.CategoryType;
import com.elearning.elearning_backend.Repository.CategoryRepository;
import com.elearning.elearning_backend.Repository.CourseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    public List<Category> getCategoriesByType(CategoryType type) {
        return categoryRepository.findByTypeAndActiveTrue(type);
    }

    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByNameAndActiveTrue(name);
    }

    public List<Category> searchCategories(String query) {
        return categoryRepository.findByDisplayNameContainingIgnoreCaseAndActiveTrue(query);
    }

    public List<Category> searchCategoriesByType(CategoryType type, String query) {
        return categoryRepository.findByTypeAndDisplayNameContainingIgnoreCaseAndActiveTrue(type, query);
    }

    public Category createCategory(Category category, String createdBy) {
        // Check if category name already exists
        if (categoryRepository.existsByNameAndActiveTrue(category.getName())) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
        }

        // Set metadata
        category.setActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category.setCreatedBy(createdBy);

        return categoryRepository.save(category);
    }

    public Category updateCategory(String id, Category updatedCategory, String updatedBy) {
        Optional<Category> existingCategory = categoryRepository.findById(id);
        if (existingCategory.isEmpty()) {
            throw new RuntimeException("Category not found");
        }

        Category category = existingCategory.get();
        
        // Update fields
        if (updatedCategory.getDisplayName() != null) {
            category.setDisplayName(updatedCategory.getDisplayName());
        }
        if (updatedCategory.getDescription() != null) {
            category.setDescription(updatedCategory.getDescription());
        }
        
        category.setUpdatedAt(LocalDateTime.now());
        
        return categoryRepository.save(category);
    }

    public void deleteCategory(String id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            throw new RuntimeException("Category not found");
        }

        Category cat = category.get();
        
        // Check if category is being used by any courses
        long coursesCount = getCourseCountByCategory(cat);
        
        if (coursesCount > 0) {
            throw new RuntimeException("Cannot delete category '" + cat.getDisplayName() + 
                    "' because it is being used by " + coursesCount + " course(s). " +
                    "Please reassign or remove the courses first.");
        }

        // Soft delete - set active to false
        cat.setActive(false);
        cat.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(cat);
    }

    public long getCourseCountByCategory(Category category) {
        // Count courses that use this category
        long count = 0;
        
        if (category.getType() == CategoryType.SKILL) {
            // Count courses where skillCategory matches this category's name
            count = courseRepository.countBySkillCategory(category.getName());
        } else if (category.getType() == CategoryType.ROLE) {
            // Count courses where jobRole matches this category's name
            count = courseRepository.countByJobRole(category.getName());
        }
        
        return count;
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByNameAndActiveTrue(name);
    }

    // Initialize default categories (run once to populate database)
    public void initializeDefaultCategories() {
        // Initialize skill categories
        if (categoryRepository.findByTypeAndActiveTrue(CategoryType.SKILL).isEmpty()) {
            createCategory(Category.builder()
                    .name("PROGRAMMING")
                    .displayName("Programming")
                    .description("General programming skills")
                    .type(CategoryType.SKILL)
                    .build(), "system");
                    
            createCategory(Category.builder()
                    .name("WEB_DEVELOPMENT")
                    .displayName("Web Development")
                    .description("Web development skills")
                    .type(CategoryType.SKILL)
                    .build(), "system");
                    
            createCategory(Category.builder()
                    .name("DATA_SCIENCE")
                    .displayName("Data Science")
                    .description("Data science and analytics")
                    .type(CategoryType.SKILL)
                    .build(), "system");
                    
            createCategory(Category.builder()
                    .name("CYBERSECURITY")
                    .displayName("Cybersecurity")
                    .description("Information security")
                    .type(CategoryType.SKILL)
                    .build(), "system");
                    
            createCategory(Category.builder()
                    .name("NETWORKING")
                    .displayName("Networking")
                    .description("Network administration")
                    .type(CategoryType.SKILL)
                    .build(), "system");
        }
        
        // Initialize job role categories
        if (categoryRepository.findByTypeAndActiveTrue(CategoryType.ROLE).isEmpty()) {
            createCategory(Category.builder()
                    .name("DEVELOPER")
                    .displayName("Developer")
                    .description("Software developer role")
                    .type(CategoryType.ROLE)
                    .build(), "system");
                    
            createCategory(Category.builder()
                    .name("DESIGNER")
                    .displayName("Designer")
                    .description("UI/UX designer role")
                    .type(CategoryType.ROLE)
                    .build(), "system");
                    
            createCategory(Category.builder()
                    .name("ANALYST")
                    .displayName("Analyst")
                    .description("Business/Data analyst role")
                    .type(CategoryType.ROLE)
                    .build(), "system");
                    
            createCategory(Category.builder()
                    .name("MANAGER")
                    .displayName("Manager")
                    .description("Project/Team manager role")
                    .type(CategoryType.ROLE)
                    .build(), "system");
        }
    }
} 