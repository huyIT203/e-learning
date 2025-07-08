package com.elearning.elearning_backend.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.elearning.elearning_backend.Model.Category;
import com.elearning.elearning_backend.Model.Category.CategoryType;
import com.elearning.elearning_backend.Service.CategoryService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dynamic-categories")
@RequiredArgsConstructor
public class DynamicCategoryController {
    private final CategoryService categoryService;

    @GetMapping("/skills")
    public ResponseEntity<Map<String, Object>> getSkillsData() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> skillsData = new HashMap<>();
        
        List<Category> categories = categoryService.getCategoriesByType(CategoryType.SKILL);
        
        for (Category category : categories) {
            Map<String, Object> categoryInfo = new HashMap<>();
            categoryInfo.put("displayName", category.getDisplayName());
            categoryInfo.put("description", category.getDescription());
            categoryInfo.put("courseCount", categoryService.getCourseCountByCategory(category));
            categoryInfo.put("id", category.getId());
            skillsData.put(category.getName(), categoryInfo);
        }
        
        response.put("skills", skillsData);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/job-roles")
    public ResponseEntity<Map<String, Object>> getJobRoles() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> rolesData = new HashMap<>();
        
        List<Category> categories = categoryService.getCategoriesByType(CategoryType.ROLE);
        
        for (Category category : categories) {
            Map<String, Object> categoryInfo = new HashMap<>();
            categoryInfo.put("displayName", category.getDisplayName());
            categoryInfo.put("description", category.getDescription());
            categoryInfo.put("courseCount", categoryService.getCourseCountByCategory(category));
            categoryInfo.put("id", category.getId());
            rolesData.put(category.getName(), categoryInfo);
        }
        
        response.put("roles", rolesData);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/job-roles-list")
    public ResponseEntity<List<Category>> getJobRolesList() {
        List<Category> categories = categoryService.getCategoriesByType(CategoryType.ROLE);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/skill-categories")
    public ResponseEntity<List<Category>> getSkillCategories() {
        List<Category> categories = categoryService.getCategoriesByType(CategoryType.SKILL);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCategoryStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Category> skillCategories = categoryService.getCategoriesByType(CategoryType.SKILL);
        List<Category> roleCategories = categoryService.getCategoriesByType(CategoryType.ROLE);
        
        stats.put("totalSkillCategories", skillCategories.size());
        stats.put("totalJobRoles", roleCategories.size());
        stats.put("totalCategories", skillCategories.size() + roleCategories.size());
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCategories(@RequestParam String query) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        Map<String, Object> skills = new HashMap<>();
        Map<String, Object> roles = new HashMap<>();
        
        int totalResults = 0;
        
        // Search in skill categories
        List<Category> skillCategories = categoryService.searchCategoriesByType(CategoryType.SKILL, query);
        for (Category category : skillCategories) {
            Map<String, Object> skillInfo = new HashMap<>();
            skillInfo.put("displayName", category.getDisplayName());
            skillInfo.put("description", category.getDescription());
            skillInfo.put("courseCount", categoryService.getCourseCountByCategory(category));
            skillInfo.put("id", category.getId());
            skills.put(category.getName(), skillInfo);
            totalResults++;
        }
        
        // Search in job roles
        List<Category> roleCategories = categoryService.searchCategoriesByType(CategoryType.ROLE, query);
        for (Category category : roleCategories) {
            Map<String, Object> roleInfo = new HashMap<>();
            roleInfo.put("displayName", category.getDisplayName());
            roleInfo.put("description", category.getDescription());
            roleInfo.put("courseCount", categoryService.getCourseCountByCategory(category));
            roleInfo.put("id", category.getId());
            roles.put(category.getName(), roleInfo);
            totalResults++;
        }
        
        results.put("skills", skills);
        results.put("roles", roles);
        response.put("results", results);
        response.put("totalResults", totalResults);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody CategoryRequest request, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Tên danh mục không được để trống");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getDisplayName() == null || request.getDisplayName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Tên hiển thị không được để trống");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getType() == null) {
                response.put("success", false);
                response.put("message", "Loại danh mục không được để trống");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if category already exists
            if (categoryService.existsByName(request.getName().toUpperCase())) {
                response.put("success", false);
                response.put("message", "Danh mục với tên này đã tồn tại");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create category
            Category category = Category.builder()
                    .name(request.getName().toUpperCase().replace(" ", "_"))
                    .displayName(request.getDisplayName())
                    .description(request.getDescription())
                    .type(CategoryType.valueOf(request.getType().toUpperCase()))
                    .build();
            
            Category savedCategory = categoryService.createCategory(category, auth.getName());
            
            response.put("success", true);
            response.put("message", "Tạo danh mục thành công");
            response.put("category", savedCategory);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable String id,
            @RequestBody CategoryRequest request,
            Authentication auth) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Category updatedCategory = Category.builder()
                    .displayName(request.getDisplayName())
                    .description(request.getDescription())
                    .build();
            
            Category savedCategory = categoryService.updateCategory(id, updatedCategory, auth.getName());
            
            response.put("success", true);
            response.put("message", "Cập nhật danh mục thành công");
            response.put("category", savedCategory);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            categoryService.deleteCategory(id);
            
            response.put("success", true);
            response.put("message", "Xóa danh mục thành công");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryInfo(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Category> categoryOpt = categoryService.getCategoryByName(id);
            if (categoryOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Danh mục không tồn tại");
                return ResponseEntity.badRequest().body(response);
            }
            
            Category category = categoryOpt.get();
            response.put("success", true);
            response.put("id", category.getId());
            response.put("name", category.getName());
            response.put("displayName", category.getDisplayName());
            response.put("description", category.getDescription());
            response.put("type", category.getType().name().toLowerCase());
            response.put("courseCount", categoryService.getCourseCountByCategory(category));
            response.put("createdAt", category.getCreatedAt());
            response.put("updatedAt", category.getUpdatedAt());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/courses/{categoryType}/{categoryName}")
    public ResponseEntity<Map<String, Object>> getCoursesByCategory(
            @PathVariable String categoryType, 
            @PathVariable String categoryName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Category> categoryOpt = categoryService.getCategoryByName(categoryName);
            if (categoryOpt.isEmpty()) {
                response.put("error", "Category not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            Category category = categoryOpt.get();
            
            // For now, return empty courses list as placeholder
            // This would need to be implemented based on how courses reference categories
            response.put("categoryType", categoryType);
            response.put("categoryName", category.getDisplayName());
            response.put("courses", new ArrayList<>());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error fetching courses: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Data
    public static class CategoryRequest {
        private String name;
        private String displayName;
        private String description;
        private String type; // "skill" or "role"
    }
} 