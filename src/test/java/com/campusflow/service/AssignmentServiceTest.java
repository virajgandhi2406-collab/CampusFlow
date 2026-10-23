package com.campusflow.service;

import com.campusflow.dto.AssignmentDto;
import com.campusflow.entity.*;
import com.campusflow.exception.AccessDeniedException;
import com.campusflow.repository.AssignmentRepository;
import com.campusflow.repository.SubjectRepository;
import com.campusflow.repository.UserRepository;
import com.campusflow.service.impl.AssignmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private UserRepository userRepository;

    private AssignmentServiceImpl assignmentService;

    private User user1;
    private User user2;
    private Subject subject1;
    private Subject subject2;

    @BeforeEach
    void setUp() {
        assignmentService = new AssignmentServiceImpl(assignmentRepository, subjectRepository, userRepository);

        user1 = new User("user1@campus.edu", "password", "User One");
        user1.setId(1L);

        user2 = new User("user2@campus.edu", "password", "User Two");
        user2.setId(2L);

        subject1 = new Subject("Math", "Calculus", "#4f46e5", user1);
        subject1.setId(10L);

        subject2 = new Subject("Physics", "Mechanics", "#0ea5e9", user2);
        subject2.setId(20L);
    }

    @Test
    @DisplayName("Should create assignment successfully when subject belongs to the user")
    void testCreateAssignment_Success() {
        AssignmentDto dto = new AssignmentDto();
        dto.setTitle("Homework 1");
        dto.setDescription("Solve problems 1-10");
        dto.setSubjectId(10L);
        dto.setDueDateTime(LocalDateTime.now().plusDays(4));
        dto.setPriority(Priority.HIGH);
        dto.setStatus(AssignmentStatus.PENDING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(subjectRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(subject1));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> {
            Assignment a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });

        Assignment result = assignmentService.createAssignment(dto, 1L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Homework 1", result.getTitle());
        assertEquals(subject1, result.getSubject());
        assertEquals(user1, result.getUser());
        assertEquals(Priority.HIGH, result.getPriority());
        assertEquals(AssignmentStatus.PENDING, result.getStatus());
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    @DisplayName("IDOR Defense: Should reject assignment creation if subject belongs to another user")
    void testCreateAssignment_UnauthorizedSubject() {
        AssignmentDto dto = new AssignmentDto();
        dto.setTitle("Hacked Assignment");
        dto.setSubjectId(20L); // Subject belonging to user2
        dto.setDueDateTime(LocalDateTime.now().plusDays(1));
        dto.setPriority(Priority.MEDIUM);
        dto.setStatus(AssignmentStatus.PENDING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(subjectRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> assignmentService.createAssignment(dto, 1L));
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    @DisplayName("IDOR Defense: Should reject accessing assignment belonging to another user")
    void testGetAssignmentById_AccessDenied() {
        Assignment otherAssignment = new Assignment(
                "Private Essay", "Confidential", subject2, user2,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(2), Priority.HIGH, AssignmentStatus.PENDING
        );
        otherAssignment.setId(500L);

        when(assignmentRepository.findById(500L)).thenReturn(Optional.of(otherAssignment));

        assertThrows(AccessDeniedException.class, () -> assignmentService.getAssignmentById(500L, 1L));
    }

    @Test
    @DisplayName("Should toggle assignment completion between PENDING and COMPLETED with timestamp")
    void testToggleAssignmentCompletion() {
        Assignment assignment = new Assignment(
                "Lab Report", "Details", subject1, user1,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(2), Priority.MEDIUM, AssignmentStatus.PENDING
        );
        assignment.setId(300L);

        when(assignmentRepository.findById(300L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Toggle from PENDING to COMPLETED
        Assignment completed = assignmentService.toggleAssignmentCompletion(300L, 1L);
        assertEquals(AssignmentStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getCompletedAt());

        // Toggle from COMPLETED back to PENDING
        Assignment reopened = assignmentService.toggleAssignmentCompletion(300L, 1L);
        assertEquals(AssignmentStatus.PENDING, reopened.getStatus());
        assertNull(reopened.getCompletedAt());
    }

    @Test
    @DisplayName("Should calculate overdue status correctly")
    void testOverdueLogic() {
        Assignment pastDue = new Assignment(
                "Late Project", "Desc", subject1, user1,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(2), Priority.HIGH, AssignmentStatus.PENDING
        );
        assertTrue(pastDue.isOverdue(), "Assignment with past due date and PENDING status should be overdue");

        // When completed, it is never overdue
        pastDue.markComplete();
        assertFalse(pastDue.isOverdue(), "Completed assignment should not be marked overdue");
    }
}
