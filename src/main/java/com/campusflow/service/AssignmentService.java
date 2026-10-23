package com.campusflow.service;

import com.campusflow.dto.AssignmentDto;
import com.campusflow.dto.AssignmentFilterDto;
import com.campusflow.entity.Assignment;
import com.campusflow.entity.AssignmentStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AssignmentService {
    Page<Assignment> getAssignments(Long userId, AssignmentFilterDto filter);
    Assignment getAssignmentById(Long id, Long userId);
    AssignmentDto getAssignmentDtoById(Long id, Long userId);
    Assignment createAssignment(AssignmentDto dto, Long userId);
    Assignment updateAssignment(Long id, AssignmentDto dto, Long userId);
    void deleteAssignment(Long id, Long userId);
    Assignment toggleAssignmentCompletion(Long id, Long userId);
    Assignment updateAssignmentStatus(Long id, AssignmentStatus status, Long userId);
    List<Assignment> getAssignmentsForSubject(Long subjectId, Long userId);
}
