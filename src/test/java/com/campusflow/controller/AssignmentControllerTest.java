package com.campusflow.controller;

import com.campusflow.dto.AssignmentDto;
import com.campusflow.dto.AssignmentFilterDto;
import com.campusflow.entity.*;
import com.campusflow.service.AssignmentService;
import com.campusflow.service.SecurityService;
import com.campusflow.service.SubjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssignmentService assignmentService;

    @MockBean
    private SubjectService subjectService;

    @MockBean
    private SecurityService securityService;

    private User currentUser;
    private Subject currentSubject;

    @BeforeEach
    void setUp() {
        currentUser = new User("tester@campus.edu", "pass123", "Test Student");
        currentUser.setId(10L);
        when(securityService.getCurrentUser()).thenReturn(currentUser);

        currentSubject = new Subject("Computer Science", "Core", "#4f46e5", currentUser);
        currentSubject.setId(1L);
    }

    @Test
    @WithMockUser(username = "tester@campus.edu", roles = {"USER"})
    @DisplayName("GET /assignments should display assignments list view with pagination")
    void testListAssignments() throws Exception {
        when(assignmentService.getAssignments(eq(10L), any(AssignmentFilterDto.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));
        when(subjectService.getSubjectEntitiesForUser(10L)).thenReturn(List.of(currentSubject));

        mockMvc.perform(get("/assignments"))
                .andExpect(status().isOk())
                .andExpect(view().name("assignments/list"))
                .andExpect(model().attributeExists("assignmentsPage"))
                .andExpect(model().attributeExists("subjects"))
                .andExpect(model().attributeExists("filter"))
                .andExpect(model().attribute("activeNav", "assignments"));
    }

    @Test
    @WithMockUser(username = "tester@campus.edu", roles = {"USER"})
    @DisplayName("GET /assignments/new should display create assignment form")
    void testShowCreateForm() throws Exception {
        when(subjectService.getSubjectEntitiesForUser(10L)).thenReturn(List.of(currentSubject));

        mockMvc.perform(get("/assignments/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("assignments/form"))
                .andExpect(model().attributeExists("assignmentDto"))
                .andExpect(model().attributeExists("subjects"))
                .andExpect(model().attribute("isEdit", false));
    }

    @Test
    @WithMockUser(username = "tester@campus.edu", roles = {"USER"})
    @DisplayName("POST /assignments should create assignment and redirect on valid input")
    void testCreateAssignment_Success() throws Exception {
        when(assignmentService.createAssignment(any(AssignmentDto.class), eq(10L)))
                .thenReturn(new Assignment());

        mockMvc.perform(post("/assignments")
                        .with(csrf())
                        .param("title", "Distributed Systems Lab 1")
                        .param("subjectId", "1")
                        .param("dueDateTime", "2026-10-30T23:59")
                        .param("priority", "HIGH")
                        .param("status", "PENDING")
                        .param("description", "Complete consensus exercises"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assignments"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(assignmentService).createAssignment(any(AssignmentDto.class), eq(10L));
    }

    @Test
    @WithMockUser(username = "tester@campus.edu", roles = {"USER"})
    @DisplayName("GET /assignments/{id} should display assignment details")
    void testViewAssignmentDetails() throws Exception {
        Assignment assignment = new Assignment(
                "Lab 1", "Desc", currentSubject, currentUser,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(3), Priority.MEDIUM, AssignmentStatus.PENDING
        );
        assignment.setId(42L);

        when(assignmentService.getAssignmentById(42L, 10L)).thenReturn(assignment);

        mockMvc.perform(get("/assignments/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("assignments/detail"))
                .andExpect(model().attributeExists("assignment"))
                .andExpect(model().attribute("activeNav", "assignments"));
    }

    @Test
    @WithMockUser(username = "tester@campus.edu", roles = {"USER"})
    @DisplayName("POST /assignments/{id}/toggle-complete should flip status and redirect")
    void testToggleCompletion() throws Exception {
        Assignment completedAssignment = new Assignment(
                "Lab 1", "Desc", currentSubject, currentUser,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(3), Priority.MEDIUM, AssignmentStatus.COMPLETED
        );
        completedAssignment.setId(42L);

        when(assignmentService.toggleAssignmentCompletion(42L, 10L)).thenReturn(completedAssignment);

        mockMvc.perform(post("/assignments/42/toggle-complete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assignments"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(assignmentService).toggleAssignmentCompletion(42L, 10L);
    }

    @Test
    @WithMockUser(username = "tester@campus.edu", roles = {"USER"})
    @DisplayName("POST /assignments/{id}/delete should remove assignment and redirect")
    void testDeleteAssignment() throws Exception {
        doNothing().when(assignmentService).deleteAssignment(42L, 10L);

        mockMvc.perform(post("/assignments/42/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assignments"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(assignmentService).deleteAssignment(42L, 10L);
    }
}
