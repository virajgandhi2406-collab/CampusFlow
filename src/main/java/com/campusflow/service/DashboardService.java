package com.campusflow.service;

import com.campusflow.dto.DashboardMetricsDto;
import com.campusflow.dto.WeeklyStatsDto;

public interface DashboardService {
    DashboardMetricsDto getDashboardMetrics(Long userId);
    WeeklyStatsDto getWeeklyProductivityStats(Long userId);
}
