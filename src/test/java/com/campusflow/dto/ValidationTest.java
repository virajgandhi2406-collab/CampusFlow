package com.campusflow.dto;

import com.campusflow.entity.AssignmentStatus;
import com.campusflow.entity.Priority;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("UserRegistrationDto: Valid registration passes validation")
    void testUserRegistrationDto_Valid() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("Emma Watson");
        dto.setEmail("emma.watson@university.edu");
        dto.setPassword("ValidPassword123");
        dto.setConfirmPassword("ValidPassword123");

        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid DTO should have no constraint violations");
        assertTrue(dto.isPasswordMatching());
    }

    @Test
    @DisplayName("UserRegistrationDto: Missing and invalid fields trigger violations")
    void testUserRegistrationDto_Invalid() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName(""); // Blank
        dto.setEmail("not-an-email"); // Malformed
        dto.setPassword("short"); // < 8 chars
        dto.setConfirmPassword("shortMismatch");

        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Invalid DTO should have constraint violations");
        assertFalse(dto.isPasswordMatching());
    }

    @Test
    @DisplayName("AssignmentDto: Valid assignment DTO passes validation")
    void testAssignmentDto_Valid() {
        AssignmentDto dto = new AssignmentDto();
        dto.setTitle("Final Research Paper");
        dto.setDescription("Detailed rubric instructions");
        dto.setSubjectId(10L);
        dto.setDueDateTime(LocalDateTime.now().plusDays(7));
        dto.setPriority(Priority.HIGH);
        dto.setStatus(AssignmentStatus.PENDING);

        Set<ConstraintViolation<AssignmentDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("AssignmentDto: Missing title, subject, or due date triggers violations")
    void testAssignmentDto_MissingRequiredFields() {
        AssignmentDto dto = new AssignmentDto();
        dto.setTitle(""); // Blank title
        dto.setSubjectId(null); // Missing subject
        dto.setDueDateTime(null); // Missing due date

        Set<ConstraintViolation<AssignmentDto>> violations = validator.validate(dto);
        assertEquals(4, violations.size(), "Should trigger @NotBlank and @Size on title, plus @NotNull on subjectId and dueDateTime");
    }

    @Test
    @DisplayName("SubjectDto: Valid subject DTO passes validation")
    void testSubjectDto_Valid() {
        SubjectDto dto = new SubjectDto(null, "Machine Learning", "Neural networks & SVMs", "#4f46e5");

        Set<ConstraintViolation<SubjectDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("SubjectDto: Blank name or invalid hex color format triggers violations")
    void testSubjectDto_InvalidNameAndColor() {
        SubjectDto dto = new SubjectDto(null, "", "Description", "invalid-color");

        Set<ConstraintViolation<SubjectDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("AssignmentFilterDto: Normalizes negative page numbers and excessively large sizes")
    void testAssignmentFilterDto_BoundaryHandling() {
        AssignmentFilterDto filter = new AssignmentFilterDto();
        filter.setPage(-5);
        filter.setSize(1000);

        assertEquals(0, filter.getPage(), "Page number should be clamped to >= 0");
        assertEquals(10, filter.getSize(), "Sizes > 50 should default to 10");

        filter.setSize(25);
        assertEquals(25, filter.getSize(), "Valid size between 1 and 50 should be preserved");
    }
}
