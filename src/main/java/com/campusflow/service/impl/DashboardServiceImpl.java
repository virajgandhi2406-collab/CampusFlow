package com.campusflow.service.impl;

import com.campusflow.dto.DashboardMetricsDto;
import com.campusflow.dto.WeeklyStatsDto;
import com.campusflow.entity.Assignment;
import com.campusflow.entity.AssignmentStatus;
import com.campusflow.repository.AssignmentRepository;
import com.campusflow.repository.SubjectRepository;
import com.campusflow.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final AssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;

    public DashboardServiceImpl(AssignmentRepository assignmentRepository,
                                SubjectRepository subjectRepository) {
        this.assignmentRepository = assignmentRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public DashboardMetricsDto getDashboardMetrics(Long userId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime next7Days = now.plusDays(7);

        long total = assignmentRepository.countByUserId(userId);
        long pending = assignmentRepository.countByUserIdAndStatus(userId, AssignmentStatus.PENDING);
        long inProgress = assignmentRepository.countByUserIdAndStatus(userId, AssignmentStatus.IN_PROGRESS);
        long completed = assignmentRepository.countByUserIdAndStatus(userId, AssignmentStatus.COMPLETED);
        long overdue = assignmentRepository.countByUserIdAndStatusNotAndDueDateBefore(userId, AssignmentStatus.COMPLETED, now);
        long upcomingCount = assignmentRepository.countByUserIdAndStatusNotAndDueDateBetween(userId, AssignmentStatus.COMPLETED, now, next7Days);
        long subjectsCount = subjectRepository.countByUserId(userId);

        double completionRate = total > 0 ? ((double) completed / total) * 100.0 : 0.0;

        List<Assignment> upcomingList = assignmentRepository.findUpcomingAssignments(userId, AssignmentStatus.COMPLETED);
        List<Assignment> overdueList = assignmentRepository.findOverdueAssignments(userId, now);

        DashboardMetricsDto dto = new DashboardMetricsDto();
        dto.setTotalAssignments(total);
        dto.setPendingAssignments(pending);
        dto.setInProgressAssignments(inProgress);
        dto.setCompletedAssignments(completed);
        dto.setOverdueAssignments(overdue);
        dto.setUpcomingAssignmentsCount(upcomingCount);
        dto.setCompletionRate(completionRate);
        dto.setTotalSubjects(subjectsCount);
        dto.setUpcomingList(upcomingList);
        dto.setOverdueList(overdueList);

        return dto;
    }

    @Override
    public WeeklyStatsDto getWeeklyProductivityStats(Long userId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        OffsetDateTime startOfWeek = monday.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfWeek = sunday.atTime(23, 59, 59, 999999999).atOffset(ZoneOffset.UTC);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // Fetch assignments completed within current week
        List<Assignment> completedThisWeek = assignmentRepository.findByUserIdAndCompletedAtBetween(userId, startOfWeek, endOfWeek);

        // Pending assignments due this week (not completed)
        long pendingThisWeekCount = assignmentRepository.countByUserIdAndStatusNotAndDueDateBetween(
                userId, AssignmentStatus.COMPLETED, startOfWeek, endOfWeek);

        // Overdue count
        long overdueCount = assignmentRepository.countByUserIdAndStatusNotAndDueDateBefore(
                userId, AssignmentStatus.COMPLETED, now);

        long completedCount = completedThisWeek.size();
        long totalWeekTasks = completedCount + pendingThisWeekCount;
        double completionRate = totalWeekTasks > 0 ? ((double) completedCount / totalWeekTasks) * 100.0 : 0.0;

        // Daily breakdown for Chart.js (Mon, Tue, Wed, Thu, Fri, Sat, Sun)
        List<String> dayLabels = new ArrayList<>();
        List<Long> dayCompletions = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE (MMM d)");

        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = monday.plusDays(i);
            dayLabels.add(currentDay.format(dayFormatter));

            long countForDay = completedThisWeek.stream()
                    .filter(a -> a.getCompletedAt() != null && a.getCompletedAt().toLocalDate().isEqual(currentDay))
                    .count();
            dayCompletions.add(countForDay);
        }

        long totalCompletedAllTime = assignmentRepository.countByUserIdAndStatus(userId, AssignmentStatus.COMPLETED);

        DateTimeFormatter rangeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

        WeeklyStatsDto dto = new WeeklyStatsDto();
        dto.setCompletedThisWeek(completedCount);
        dto.setPendingThisWeek(pendingThisWeekCount);
        dto.setOverdueCount(overdueCount);
        dto.setCompletionRate(completionRate);
        dto.setDayLabels(dayLabels);
        dto.setDayCompletions(dayCompletions);
        dto.setWeekStartFormatted(monday.format(rangeFormatter));
        dto.setWeekEndFormatted(sunday.format(rangeFormatter));
        dto.setTotalCompletedAllTime(totalCompletedAllTime);

        return dto;
    }
}
