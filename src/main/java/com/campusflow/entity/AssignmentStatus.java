package com.campusflow.entity;

public enum AssignmentStatus {
    PENDING("Pending", "badge-pending"),
    IN_PROGRESS("In Progress", "badge-in-progress"),
    COMPLETED("Completed", "badge-completed");

    private final String displayName;
    private final String badgeClass;

    AssignmentStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
