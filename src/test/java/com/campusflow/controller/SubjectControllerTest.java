package com.campusflow.controller;

import com.campusflow.dto.SubjectDto;
import com.campusflow.entity.Subject;
import com.campusflow.entity.User;
import com.campusflow.exception.SubjectConflictException;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubjectService subjectService;

    @MockBean
    private AssignmentService assignmentService;

    @MockBean
    private SecurityService securityService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User("tester@campus.edu", "pass123", "Test Student");
        currentUser.setId(10L);
        when(securityService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    @WithMockUser(username = "tester@campus.edu", roles = {"USER"})
    @DisplayName("GET /subjects should display subjects list view")
    void testListSubjects() throws Exception {
        SubjectDto dto = new SubjectDto(1L, "Algorithms", "Advanced Graphs", "#4f46e5");
        dto.setAssignmentCount(2L);
        when(subjectService.getSubjectsForUser(10L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/subjects"))
                .andExpect(status().isOk())
                .andExpect(view().name("subjects/list"))
                .andExpect(model().attributeExists("subjects"))
                .andExpect(model().attribute("activeNav", "subjects"));
    }

    @Test
    @WithMockUser(username = "tester@campus.edu", roles = {"USER"})
    @DisplayName("GET /subjects/new should display create subject form")
    void testShowCreateForm() throws Exception {
        mockMvc.perform(get("/subjects/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("subjects/form"))
                .andExpect(model().attributeExists("subjectDto"))
                .andExpect(model().attribute("isEdit", false));
    }

    @Test
    @WithMockUser(username = "tester@campus.edu", roles = {"USER"})
    @DisplayName("POST /subjects should create subject and redirect on valid input")
    void testCreateSubject_Success() throws Exception {
        when(subjectService.createSubject(any(SubjectDto.class), eq(10L))).thenReturn(new Subject());

        mockMvc.perform(post("/subjects")
                        .with(csrf())
                        .param("name", "Computer Networks")
                        .param("color", "#0ea5e9")
                        .param("description", "TCP/IP and OSI Model"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/subjects"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(subjectService).createSubject(any(SubjectDto.class), eq(10L));
    }

    @Test
    @WithMockUser(username = "tester@campus.edu", roles = {"USER"})
    @DisplayName("POST /subjects should return form with error on duplicate subject name")
    void testCreateSubject_DuplicateError() throws Exception {
        when(subjectService.createSubject(any(SubjectDto.class), eq(10L)))
                .thenThrow(new SubjectConflictException("A subject with this name already exists."));

        mockMvc.perform(post("/subjects")
                        .with(csrf())
                        .param("name", "Computer Networks")
                        .param("color", "#0ea5e9"))
                .andExpect(status().isOk())
                .andExpect(view().name("subjects/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "tester@campus.edu", roles = {"USER"})
    @DisplayName("GET /subjects/{id} should display subject details")
    void testViewSubjectDetails() throws Exception {
        Subject subject = new Subject("Databases", "Relational design", "#10b981", currentUser);
        subject.setId(5L);

        when(subjectService.getSubjectById(5L, 10L)).thenReturn(subject);
        when(assignmentService.getAssignmentsForSubject(5L, 10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/subjects/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("subjects/detail"))
                .andExpect(model().attributeExists("subject"))
                .andExpect(model().attributeExists("assignments"));
    }

    @Test
    @WithMockUser(username = "tester@campus.edu", roles = {"USER"})
    @DisplayName("POST /subjects/{id}/delete should delete empty subject and redirect")
    void testDeleteSubject_Success() throws Exception {
        doNothing().when(subjectService).deleteSubject(5L, 10L);

        mockMvc.perform(post("/subjects/5/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/subjects"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(subjectService).deleteSubject(5L, 10L);
    }
}
