package com.campusflow.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "due_date", nullable = false)
    private OffsetDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Assignment() {
    }

    public Assignment(String title, String description, Subject subject, User user,
                      OffsetDateTime dueDate, Priority priority, AssignmentStatus status) {
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.user = user;
        this.dueDate = dueDate;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.status = status != null ? status : AssignmentStatus.PENDING;
        if (this.status == AssignmentStatus.COMPLETED) {
            this.completedAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = this.createdAt;
        if (this.priority == null) {
            this.priority = Priority.MEDIUM;
        }
        if (this.status == null) {
            this.status = AssignmentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public boolean isOverdue() {
        if (this.status == AssignmentStatus.COMPLETED) {
            return false;
        }
        return this.dueDate != null && this.dueDate.isBefore(OffsetDateTime.now(ZoneOffset.UTC));
    }

    public boolean isDueSoon() {
        if (this.status == AssignmentStatus.COMPLETED || isOverdue()) {
            return false;
        }
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime inSevenDays = now.plusDays(7);
        return this.dueDate != null && !this.dueDate.isBefore(now) && !this.dueDate.isAfter(inSevenDays);
    }

    public void markComplete() {
        this.status = AssignmentStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void markPending() {
        this.status = AssignmentStatus.PENDING;
        this.completedAt = null;
    }

    public void markInProgress() {
        this.status = AssignmentStatus.IN_PROGRESS;
        this.completedAt = null;
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
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OffsetDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(OffsetDateTime dueDate) {
        this.dueDate = dueDate;
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
        if (status == AssignmentStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = OffsetDateTime.now(ZoneOffset.UTC);
        } else if (status != AssignmentStatus.COMPLETED) {
            this.completedAt = null;
        }
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", dueDate=" + dueDate +
                '}';
    }
}
