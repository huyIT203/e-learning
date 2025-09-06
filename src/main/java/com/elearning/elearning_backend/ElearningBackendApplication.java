package com.elearning.elearning_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.elearning.elearning_backend.Service.CategoryService;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class ElearningBackendApplication {

	@Autowired
	private CategoryService categoryService;

	public static void main(String[] args) {
		SpringApplication.run(ElearningBackendApplication.class, args);
	}

	@PostConstruct
	public void initializeData() {
		try {
			categoryService.initializeDefaultCategories();
			System.out.println(" Default categories initialized successfully");
		} catch (Exception e) {
			System.err.println("Error initializing default categories: " + e.getMessage());
		}
	}
}
