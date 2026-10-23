package com.campusflow.dto;

import com.campusflow.entity.AssignmentStatus;
import com.campusflow.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class AssignmentDto {

    private Long id;

    @NotBlank(message = "Assignment title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    @Size(max = 3000, message = "Description cannot exceed 3000 characters")
    private String description;

    @NotNull(message = "Please select a subject")
    private Long subjectId;

    private String subjectName;
    private String subjectColor;

    @NotNull(message = "Due date and time is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dueDateTime;

    @NotNull(message = "Please select a priority level")
    private Priority priority = Priority.MEDIUM;

    @NotNull(message = "Please select a status")
    private AssignmentStatus status = AssignmentStatus.PENDING;

    public AssignmentDto() {
    }

    public AssignmentDto(Long id, String title, String description, Long subjectId,
                         LocalDateTime dueDateTime, Priority priority, AssignmentStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.subjectId = subjectId;
        this.dueDateTime = dueDateTime;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.status = status != null ? status : AssignmentStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectColor() {
        return subjectColor;
    }

    public void setSubjectColor(String subjectColor) {
        this.subjectColor = subjectColor;
    }

    public LocalDateTime getDueDateTime() {
        return dueDateTime;
    }

    public void setDueDateTime(LocalDateTime dueDateTime) {
        this.dueDateTime = dueDateTime;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }
}
