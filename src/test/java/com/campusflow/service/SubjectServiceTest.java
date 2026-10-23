package com.campusflow.service;

import com.campusflow.dto.SubjectDto;
import com.campusflow.entity.Subject;
import com.campusflow.entity.User;
import com.campusflow.exception.AccessDeniedException;
import com.campusflow.exception.SubjectConflictException;
import com.campusflow.repository.AssignmentRepository;
import com.campusflow.repository.SubjectRepository;
import com.campusflow.repository.UserRepository;
import com.campusflow.service.impl.SubjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    private SubjectServiceImpl subjectService;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        subjectService = new SubjectServiceImpl(subjectRepository, assignmentRepository, userRepository);

        testUser = new User("student@campus.edu", "pass123", "Alice Student");
        testUser.setId(10L);

        otherUser = new User("other@campus.edu", "pass123", "Bob Student");
        otherUser.setId(20L);
    }

    @Test
    @DisplayName("Should create subject successfully when name is unique for user")
    void testCreateSubject_Success() {
        SubjectDto dto = new SubjectDto(null, "Data Structures", "Course notes", "#4f46e5");

        when(subjectRepository.existsByUserIdAndNameIgnoreCase(10L, "Data Structures")).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(subjectRepository.save(any(Subject.class))).thenAnswer(inv -> {
            Subject s = inv.getArgument(0);
            s.setId(100L);
            return s;
        });

        Subject result = subjectService.createSubject(dto, 10L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Data Structures", result.getName());
        assertEquals(testUser, result.getUser());
        verify(subjectRepository).save(any(Subject.class));
    }

    @Test
    @DisplayName("Should throw SubjectConflictException when subject name already exists for user")
    void testCreateSubject_DuplicateName() {
        SubjectDto dto = new SubjectDto(null, "Data Structures", null, "#4f46e5");

        when(subjectRepository.existsByUserIdAndNameIgnoreCase(10L, "Data Structures")).thenReturn(true);

        assertThrows(SubjectConflictException.class, () -> subjectService.createSubject(dto, 10L));
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when accessing another user's subject (IDOR check)")
    void testGetSubjectById_Unauthorized() {
        Subject otherSubject = new Subject("Databases", "Other notes", "#10b981", otherUser);
        otherSubject.setId(200L);

        when(subjectRepository.findById(200L)).thenReturn(Optional.of(otherSubject));

        assertThrows(AccessDeniedException.class, () -> subjectService.getSubjectById(200L, 10L));
    }

    @Test
    @DisplayName("Safe Deletion: Should throw SubjectConflictException and block deletion when subject has assignments")
    void testDeleteSubject_BlockedWhenHasAssignments() {
        Subject subject = new Subject("Algorithms", "Notes", "#3b82f6", testUser);
        subject.setId(300L);

        when(subjectRepository.findById(300L)).thenReturn(Optional.of(subject));
        when(assignmentRepository.countBySubjectId(300L)).thenReturn(3L);

        SubjectConflictException ex = assertThrows(SubjectConflictException.class,
                () -> subjectService.deleteSubject(300L, 10L));

        assertTrue(ex.getMessage().contains("contains 3 assignment(s)"));
        verify(subjectRepository, never()).delete(any(Subject.class));
    }

    @Test
    @DisplayName("Safe Deletion: Should delete subject successfully when it has 0 assignments")
    void testDeleteSubject_SuccessWhenNoAssignments() {
        Subject subject = new Subject("Elective Seminar", "Notes", "#3b82f6", testUser);
        subject.setId(301L);

        when(subjectRepository.findById(301L)).thenReturn(Optional.of(subject));
        when(assignmentRepository.countBySubjectId(301L)).thenReturn(0L);

        assertDoesNotThrow(() -> subjectService.deleteSubject(301L, 10L));
        verify(subjectRepository).delete(subject);
    }
}
