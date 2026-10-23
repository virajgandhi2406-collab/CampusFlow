package com.campusflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SubjectDto {

    private Long id;

    @NotBlank(message = "Subject name is required")
    @Size(min = 2, max = 100, message = "Subject name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Please select a valid hex color code (e.g. #4F46E5)")
    private String color = "#4f46e5";

    private long assignmentCount;

    public SubjectDto() {
    }

    public SubjectDto(Long id, String name, String description, String color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = (color != null && !color.isBlank()) ? color : "#4f46e5";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getAssignmentCount() {
        return assignmentCount;
    }

    public void setAssignmentCount(long assignmentCount) {
        this.assignmentCount = assignmentCount;
    }
}
