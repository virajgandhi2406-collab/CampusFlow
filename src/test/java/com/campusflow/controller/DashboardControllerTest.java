package com.campusflow.controller;

import com.campusflow.dto.DashboardMetricsDto;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private SecurityService securityService;

    @Test
    @WithMockUser(username = "student@campus.edu", roles = {"USER"})
    @DisplayName("Root URL / should redirect authenticated users to /dashboard")
    void testRootRedirect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @WithMockUser(username = "student@campus.edu", roles = {"USER"})
    @DisplayName("Dashboard endpoint /dashboard should render dashboard/index view with model attributes")
    void testShowDashboard() throws Exception {
        User mockUser = new User("student@campus.edu", "encodedPassword", "Student One");
        mockUser.setId(10L);

        DashboardMetricsDto mockMetrics = new DashboardMetricsDto();
        mockMetrics.setTotalAssignments(5L);
        mockMetrics.setCompletedAssignments(3L);
        mockMetrics.setPendingAssignments(2L);
        mockMetrics.setCompletionRate(60.0);

        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(dashboardService.getDashboardMetrics(10L)).thenReturn(mockMetrics);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("metrics"))
                .andExpect(model().attribute("activeNav", "dashboard"));
    }
}
