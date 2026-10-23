package com.campusflow.entity;

public enum Priority {
    LOW("Low", "badge-low"),
    MEDIUM("Medium", "badge-medium"),
    HIGH("High", "badge-high");

    private final String displayName;
    private final String badgeClass;

    Priority(String displayName, String badgeClass) {
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
