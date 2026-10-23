package com.campusflow.controller;

import com.campusflow.dto.WeeklyStatsDto;
import com.campusflow.entity.User;
import com.campusflow.service.DashboardService;
import com.campusflow.service.SecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InsightsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private SecurityService securityService;

    @Test
    @WithMockUser(username = "student@campus.edu", roles = {"USER"})
    @DisplayName("Insights endpoint /insights should render insights/index view with weekly stats")
    void testShowProductivityInsights() throws Exception {
        User mockUser = new User("student@campus.edu", "password", "Active Student");
        mockUser.setId(15L);

        WeeklyStatsDto statsDto = new WeeklyStatsDto();
        statsDto.setCompletedThisWeek(4L);
        statsDto.setPendingThisWeek(1L);
        statsDto.setOverdueCount(0L);
        statsDto.setCompletionRate(80.0);
        statsDto.setDayLabels(List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));
        statsDto.setDayCompletions(List.of(1L, 0L, 2L, 1L, 0L, 0L, 0L));
        statsDto.setWeekStartFormatted("Oct 19, 2026");
        statsDto.setWeekEndFormatted("Oct 25, 2026");

        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(dashboardService.getWeeklyProductivityStats(15L)).thenReturn(statsDto);

        mockMvc.perform(get("/insights"))
                .andExpect(status().isOk())
                .andExpect(view().name("insights/index"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("activeNav", "insights"));
    }
}
