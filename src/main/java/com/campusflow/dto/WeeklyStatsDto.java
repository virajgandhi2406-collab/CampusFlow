package com.campusflow.dto;

import java.util.ArrayList;
import java.util.List;

public class WeeklyStatsDto {

    private long completedThisWeek;
    private long pendingThisWeek;
    private long overdueCount;
    private double completionRate;
    private String weekStartFormatted;
    private String weekEndFormatted;
    private List<String> dayLabels = new ArrayList<>();
    private List<Long> dayCompletions = new ArrayList<>();
    private long totalCompletedAllTime;

    public WeeklyStatsDto() {
    }

    public long getCompletedThisWeek() {
        return completedThisWeek;
    }

    public void setCompletedThisWeek(long completedThisWeek) {
        this.completedThisWeek = completedThisWeek;
    }

    public long getPendingThisWeek() {
        return pendingThisWeek;
    }

    public void setPendingThisWeek(long pendingThisWeek) {
        this.pendingThisWeek = pendingThisWeek;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = Math.round(completionRate * 10.0) / 10.0;
    }

    public String getWeekStartFormatted() {
        return weekStartFormatted;
    }

    public void setWeekStartFormatted(String weekStartFormatted) {
        this.weekStartFormatted = weekStartFormatted;
    }

    public String getWeekEndFormatted() {
        return weekEndFormatted;
    }

    public void setWeekEndFormatted(String weekEndFormatted) {
        this.weekEndFormatted = weekEndFormatted;
    }

    public List<String> getDayLabels() {
        return dayLabels;
    }

    public void setDayLabels(List<String> dayLabels) {
        this.dayLabels = dayLabels;
    }

    public List<Long> getDayCompletions() {
        return dayCompletions;
    }

    public void setDayCompletions(List<Long> dayCompletions) {
        this.dayCompletions = dayCompletions;
    }

    public long getTotalCompletedAllTime() {
        return totalCompletedAllTime;
    }

    public void setTotalCompletedAllTime(long totalCompletedAllTime) {
        this.totalCompletedAllTime = totalCompletedAllTime;
    }

    public String getDayLabelsJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < dayLabels.size(); i++) {
            sb.append("\"").append(dayLabels.get(i).replace("\"", "\\\"")).append("\"");
            if (i < dayLabels.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public String getDayCompletionsJson() {
        return dayCompletions != null ? dayCompletions.toString() : "[]";
    }
}
