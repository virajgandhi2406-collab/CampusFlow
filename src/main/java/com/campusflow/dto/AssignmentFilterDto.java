package com.campusflow.dto;

import com.campusflow.entity.Priority;

public class AssignmentFilterDto {

    private String search;
    private Long subjectId;
    private String status = "ALL"; // ALL, PENDING, IN_PROGRESS, COMPLETED, OVERDUE
    private Priority priority;
    private String sortBy = "dueDateAsc"; // dueDateAsc, dueDateDesc, priorityDesc, priorityAsc, titleAsc
    private int page = 0;
    private int size = 10;

    public AssignmentFilterDto() {
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = (search != null && !search.isBlank()) ? search.trim() : null;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status != null && !status.isBlank()) ? status.trim().toUpperCase() : "ALL";
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy.trim() : "dueDateAsc";
    }

    public int getPage() {
        return Math.max(0, page);
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public int getSize() {
        return (size > 0 && size <= 50) ? size : 10;
    }

    public void setSize(int size) {
        this.size = (size > 0 && size <= 50) ? size : 10;
    }
}
