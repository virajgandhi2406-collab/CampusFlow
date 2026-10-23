package com.campusflow.dto;

import com.campusflow.entity.Assignment;
import java.util.ArrayList;
import java.util.List;

public class DashboardMetricsDto {

    private long totalAssignments;
    private long pendingAssignments;
    private long inProgressAssignments;
    private long completedAssignments;
    private long overdueAssignments;
    private long upcomingAssignmentsCount;
    private double completionRate;
    private long totalSubjects;
    private List<Assignment> upcomingList = new ArrayList<>();
    private List<Assignment> overdueList = new ArrayList<>();

    public DashboardMetricsDto() {
    }

    public boolean hasData() {
        return totalAssignments > 0 || totalSubjects > 0;
    }

    public long getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(long totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public long getPendingAssignments() {
        return pendingAssignments;
    }

    public void setPendingAssignments(long pendingAssignments) {
        this.pendingAssignments = pendingAssignments;
    }

    public long getInProgressAssignments() {
        return inProgressAssignments;
    }

    public void setInProgressAssignments(long inProgressAssignments) {
        this.inProgressAssignments = inProgressAssignments;
    }

    public long getCompletedAssignments() {
        return completedAssignments;
    }

    public void setCompletedAssignments(long completedAssignments) {
        this.completedAssignments = completedAssignments;
    }

    public long getOverdueAssignments() {
        return overdueAssignments;
    }

    public void setOverdueAssignments(long overdueAssignments) {
        this.overdueAssignments = overdueAssignments;
    }

    public long getUpcomingAssignmentsCount() {
        return upcomingAssignmentsCount;
    }

    public void setUpcomingAssignmentsCount(long upcomingAssignmentsCount) {
        this.upcomingAssignmentsCount = upcomingAssignmentsCount;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = Math.round(completionRate * 10.0) / 10.0;
    }

    public long getTotalSubjects() {
        return totalSubjects;
    }

    public void setTotalSubjects(long totalSubjects) {
        this.totalSubjects = totalSubjects;
    }

    public List<Assignment> getUpcomingList() {
        return upcomingList;
    }

    public void setUpcomingList(List<Assignment> upcomingList) {
        this.upcomingList = upcomingList;
    }

    public List<Assignment> getOverdueList() {
        return overdueList;
    }

    public void setOverdueList(List<Assignment> overdueList) {
        this.overdueList = overdueList;
    }
}
