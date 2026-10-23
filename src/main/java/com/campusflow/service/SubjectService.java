package com.campusflow.service;

import com.campusflow.dto.SubjectDto;
import com.campusflow.entity.Subject;

import java.util.List;

public interface SubjectService {
    List<SubjectDto> getSubjectsForUser(Long userId);
    List<Subject> getSubjectEntitiesForUser(Long userId);
    Subject getSubjectById(Long id, Long userId);
    SubjectDto getSubjectDtoById(Long id, Long userId);
    Subject createSubject(SubjectDto dto, Long userId);
    Subject updateSubject(Long id, SubjectDto dto, Long userId);
    void deleteSubject(Long id, Long userId);
    long countAssignmentsForSubject(Long subjectId);
}
