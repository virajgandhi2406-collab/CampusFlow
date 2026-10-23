package com.campusflow.security;

import com.campusflow.entity.*;
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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAndIsolationTest {

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

    private User alice;
    private User bob;
    private Subject bobSubject;
    private Assignment bobAssignment;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
        subjectRepository.deleteAll();
        userRepository.deleteAll();

        alice = new User("alice@university.edu", passwordEncoder.encode("password123"), "Alice Smith");
        alice = userRepository.save(alice);

        bob = new User("bob@university.edu", passwordEncoder.encode("password123"), "Bob Jones");
        bob = userRepository.save(bob);

        bobSubject = new Subject("Bob's Private Physics", "Class notes", "#4f46e5", bob);
        bobSubject = subjectRepository.save(bobSubject);

        bobAssignment = new Assignment(
                "Bob's Quantum Lab", "Secret lab work", bobSubject, bob,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(5), Priority.HIGH, AssignmentStatus.PENDING
        );
        bobAssignment = assignmentRepository.save(bobAssignment);
    }

    @Test
    @DisplayName("Security: Unauthenticated request to /assignments must redirect to /login")
    void testUnauthenticatedRedirect() throws Exception {
        mockMvc.perform(get("/assignments"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Security: POST without CSRF token must be forbidden (HTTP 403)")
    void testCsrfProtectionEnforced() throws Exception {
        mockMvc.perform(post("/assignments")
                        .param("title", "Test without CSRF"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice@university.edu", roles = {"USER"})
    @DisplayName("Data Isolation: Alice must NOT be able to view Bob's assignment (IDOR defense)")
    void testAssignmentIsolation() throws Exception {
        mockMvc.perform(get("/assignments/" + bobAssignment.getId()))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));
    }

    @Test
    @WithMockUser(username = "alice@university.edu", roles = {"USER"})
    @DisplayName("Data Isolation: Alice must NOT be able to access Bob's subject details (IDOR defense)")
    void testSubjectIsolation() throws Exception {
        mockMvc.perform(get("/subjects/" + bobSubject.getId()))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));
    }

    @Test
    @WithMockUser(username = "alice@university.edu", roles = {"USER"})
    @DisplayName("Data Isolation: Alice must NOT be able to delete Bob's assignment")
    void testDeleteAssignmentIsolation() throws Exception {
        mockMvc.perform(post("/assignments/" + bobAssignment.getId() + "/delete")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));
    }
}
