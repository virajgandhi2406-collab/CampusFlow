package com.campusflow.service;

import com.campusflow.dto.DashboardMetricsDto;
import com.campusflow.dto.WeeklyStatsDto;
import com.campusflow.entity.*;
import com.campusflow.repository.AssignmentRepository;
import com.campusflow.repository.SubjectRepository;
import com.campusflow.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(assignmentRepository, subjectRepository);
    }

    @Test
    @DisplayName("Should derive dashboard metrics accurately from database query results")
    void testGetDashboardMetrics() {
        Long userId = 5L;

        when(assignmentRepository.countByUserId(userId)).thenReturn(10L);
        when(assignmentRepository.countByUserIdAndStatus(userId, AssignmentStatus.PENDING)).thenReturn(4L);
        when(assignmentRepository.countByUserIdAndStatus(userId, AssignmentStatus.IN_PROGRESS)).thenReturn(2L);
        when(assignmentRepository.countByUserIdAndStatus(userId, AssignmentStatus.COMPLETED)).thenReturn(4L);
        when(assignmentRepository.countByUserIdAndStatusNotAndDueDateBefore(eq(userId), eq(AssignmentStatus.COMPLETED), any(OffsetDateTime.class))).thenReturn(1L);
        when(assignmentRepository.countByUserIdAndStatusNotAndDueDateBetween(eq(userId), eq(AssignmentStatus.COMPLETED), any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(3L);
        when(subjectRepository.countByUserId(userId)).thenReturn(3L);
        when(assignmentRepository.findUpcomingAssignments(eq(userId), eq(AssignmentStatus.COMPLETED))).thenReturn(Collections.emptyList());
        when(assignmentRepository.findOverdueAssignments(eq(userId), any(OffsetDateTime.class))).thenReturn(Collections.emptyList());

        DashboardMetricsDto metrics = dashboardService.getDashboardMetrics(userId);

        assertEquals(10L, metrics.getTotalAssignments());
        assertEquals(4L, metrics.getPendingAssignments());
        assertEquals(2L, metrics.getInProgressAssignments());
        assertEquals(4L, metrics.getCompletedAssignments());
        assertEquals(1L, metrics.getOverdueAssignments());
        assertEquals(3L, metrics.getUpcomingAssignmentsCount());
        assertEquals(3L, metrics.getTotalSubjects());
        assertEquals(40.0, metrics.getCompletionRate(), 0.01);
        assertTrue(metrics.hasData());
    }

    @Test
    @DisplayName("Should return 0% completion rate when no assignments exist")
    void testGetDashboardMetrics_ZeroAssignments() {
        Long userId = 6L;

        when(assignmentRepository.countByUserId(userId)).thenReturn(0L);
        when(assignmentRepository.countByUserIdAndStatus(userId, AssignmentStatus.PENDING)).thenReturn(0L);
        when(assignmentRepository.countByUserIdAndStatus(userId, AssignmentStatus.IN_PROGRESS)).thenReturn(0L);
        when(assignmentRepository.countByUserIdAndStatus(userId, AssignmentStatus.COMPLETED)).thenReturn(0L);
        when(assignmentRepository.countByUserIdAndStatusNotAndDueDateBefore(eq(userId), eq(AssignmentStatus.COMPLETED), any())).thenReturn(0L);
        when(assignmentRepository.countByUserIdAndStatusNotAndDueDateBetween(eq(userId), eq(AssignmentStatus.COMPLETED), any(), any())).thenReturn(0L);
        when(subjectRepository.countByUserId(userId)).thenReturn(0L);

        DashboardMetricsDto metrics = dashboardService.getDashboardMetrics(userId);

        assertEquals(0L, metrics.getTotalAssignments());
        assertEquals(0.0, metrics.getCompletionRate());
        assertFalse(metrics.hasData());
    }

    @Test
    @DisplayName("Should calculate weekly productivity statistics and daily counts for chart")
    void testGetWeeklyProductivityStats() {
        Long userId = 7L;
        User user = new User("stats@campus.edu", "pass", "Student");
        Subject subject = new Subject("Biology", "Bio", "#10b981", user);

        Assignment a1 = new Assignment("Lab 1", "Desc", subject, user, OffsetDateTime.now(ZoneOffset.UTC), Priority.MEDIUM, AssignmentStatus.COMPLETED);
        a1.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));

        when(assignmentRepository.findByUserIdAndCompletedAtBetween(eq(userId), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(a1));
        when(assignmentRepository.countByUserIdAndStatusNotAndDueDateBetween(eq(userId), eq(AssignmentStatus.COMPLETED), any(), any()))
                .thenReturn(2L);
        when(assignmentRepository.countByUserIdAndStatusNotAndDueDateBefore(eq(userId), eq(AssignmentStatus.COMPLETED), any()))
                .thenReturn(0L);
        when(assignmentRepository.countByUserIdAndStatus(userId, AssignmentStatus.COMPLETED))
                .thenReturn(5L);

        WeeklyStatsDto stats = dashboardService.getWeeklyProductivityStats(userId);

        assertEquals(1L, stats.getCompletedThisWeek());
        assertEquals(2L, stats.getPendingThisWeek());
        assertEquals(0L, stats.getOverdueCount());
        assertEquals(5L, stats.getTotalCompletedAllTime());
        // 1 completed / (1 completed + 2 pending) = 33.3%
        assertEquals(33.3, stats.getCompletionRate(), 0.1);
        assertEquals(7, stats.getDayLabels().size());
        assertEquals(7, stats.getDayCompletions().size());
    }
}
