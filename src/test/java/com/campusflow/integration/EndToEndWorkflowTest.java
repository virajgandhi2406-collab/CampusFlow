package com.campusflow.integration;

import com.campusflow.entity.Assignment;
import com.campusflow.entity.AssignmentStatus;
import com.campusflow.entity.Subject;
import com.campusflow.entity.User;
import com.campusflow.repository.AssignmentRepository;
import com.campusflow.repository.SubjectRepository;
import com.campusflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EndToEndWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        assignmentRepository.deleteAll();
        subjectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Complete Student Lifecycle: Registration, Coursework creation, Completion, Insights, and Safe Deletion")
    void testCompleteStudentLifecycle() throws Exception {
        // Step 1: Registration
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Samantha Reed")
                        .param("email", "samantha@university.edu")
                        .param("password", "SecurePassword999")
                        .param("confirmPassword", "SecurePassword999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        User savedUser = userRepository.findByEmailIgnoreCase("samantha@university.edu").orElseThrow();
        assertEquals("Samantha Reed", savedUser.getFullName());
        assertTrue(passwordEncoder.matches("SecurePassword999", savedUser.getPassword()));
        assertNotEquals("SecurePassword999", savedUser.getPassword(), "Plaintext password must never be stored");

        // Verify duplicate registration attempt is rejected
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Samantha Reed")
                        .param("email", "samantha@university.edu")
                        .param("password", "SecurePassword999")
                        .param("confirmPassword", "SecurePassword999"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "samantha@university.edu", roles = {"USER"})
    @DisplayName("End-to-End Coursework Workflow with Safe Deletion Policies")
    void testCourseworkWorkflowAndSafeDeletion() throws Exception {
        // Seed user for authenticated session
        User user = new User("samantha@university.edu", passwordEncoder.encode("Pass1234"), "Samantha Reed");
        user = userRepository.save(user);

        // Step 2: Create Subject
        mockMvc.perform(post("/subjects")
                        .with(csrf())
                        .param("name", "CS 401 — Artificial Intelligence")
                        .param("color", "#4f46e5")
                        .param("description", "Search algorithms and knowledge graphs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/subjects"));

        List<Subject> subjects = subjectRepository.findByUserIdOrderByNameAsc(user.getId());
        assertEquals(1, subjects.size());
        Subject subject = subjects.get(0);
        assertEquals("CS 401 — Artificial Intelligence", subject.getName());

        // Step 3: Create Assignment
        String dueDateStr = LocalDateTime.now().plusDays(4).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        mockMvc.perform(post("/assignments")
                        .with(csrf())
                        .param("title", "A* Pathfinding Project")
                        .param("subjectId", subject.getId().toString())
                        .param("dueDateTime", dueDateStr)
                        .param("priority", "HIGH")
                        .param("status", "PENDING")
                        .param("description", "Implement heuristics and grid benchmark"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assignments"));

        List<Assignment> assignments = assignmentRepository.findByUserIdAndSubjectIdOrderByDueDateAsc(user.getId(), subject.getId());
        assertEquals(1, assignments.size());
        Assignment assignment = assignments.get(0);
        assertEquals("A* Pathfinding Project", assignment.getTitle());
        assertEquals(AssignmentStatus.PENDING, assignment.getStatus());
        assertNull(assignment.getCompletedAt());

        // Step 4: Verify Dashboard reflects live database data
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attributeExists("metrics"));

        // Step 5: Mark Assignment Completed
        mockMvc.perform(post("/assignments/" + assignment.getId() + "/toggle-complete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Assignment updatedAssignment = assignmentRepository.findById(assignment.getId()).orElseThrow();
        assertEquals(AssignmentStatus.COMPLETED, updatedAssignment.getStatus());
        assertNotNull(updatedAssignment.getCompletedAt(), "completed_at timestamp must be recorded");

        // Step 6: Verify Insights View renders cleanly with updated completed stats
        mockMvc.perform(get("/insights"))
                .andExpect(status().isOk())
                .andExpect(view().name("insights/index"))
                .andExpect(model().attributeExists("stats"));

        // Step 7: Attempt to delete Subject containing coursework (Safe Deletion Policy test)
        mockMvc.perform(post("/subjects/" + subject.getId() + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/subjects"))
                .andExpect(flash().attributeExists("errorMessage"));

        assertTrue(subjectRepository.existsById(subject.getId()), "Subject with active assignments must NOT be deleted");

        // Step 8: Delete Assignment
        mockMvc.perform(post("/assignments/" + assignment.getId() + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assignments"));

        assertFalse(assignmentRepository.existsById(assignment.getId()));

        // Step 9: Now delete Subject with 0 assignments (must succeed)
        mockMvc.perform(post("/subjects/" + subject.getId() + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/subjects"))
                .andExpect(flash().attributeExists("successMessage"));

        assertFalse(subjectRepository.existsById(subject.getId()), "Subject without assignments must be deleted successfully");
    }
}
