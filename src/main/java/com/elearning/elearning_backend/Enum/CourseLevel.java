package com.elearning.elearning_backend.Enum;

public enum CourseLevel {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"), 
    ADVANCED("Advanced"),
    EXPERT("Expert");

    private final String displayName;

    CourseLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 