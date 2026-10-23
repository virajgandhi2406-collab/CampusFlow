package com.campusflow.service.impl;

import com.campusflow.dto.SubjectDto;
import com.campusflow.entity.Subject;
import com.campusflow.entity.User;
import com.campusflow.exception.AccessDeniedException;
import com.campusflow.exception.ResourceNotFoundException;
import com.campusflow.exception.SubjectConflictException;
import com.campusflow.repository.AssignmentRepository;
import com.campusflow.repository.SubjectRepository;
import com.campusflow.repository.UserRepository;
import com.campusflow.service.SubjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository,
                              AssignmentRepository assignmentRepository,
                              UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectDto> getSubjectsForUser(Long userId) {
        List<Subject> subjects = subjectRepository.findByUserIdOrderByNameAsc(userId);
        return subjects.stream().map(subject -> {
            SubjectDto dto = new SubjectDto(subject.getId(), subject.getName(), subject.getDescription(), subject.getColor());
            dto.setAssignmentCount(assignmentRepository.countBySubjectId(subject.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> getSubjectEntitiesForUser(Long userId) {
        return subjectRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Subject getSubjectById(Long id, Long userId) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + id));

        if (!subject.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to access this subject");
        }
        return subject;
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectDto getSubjectDtoById(Long id, Long userId) {
        Subject subject = getSubjectById(id, userId);
        SubjectDto dto = new SubjectDto(subject.getId(), subject.getName(), subject.getDescription(), subject.getColor());
        dto.setAssignmentCount(assignmentRepository.countBySubjectId(subject.getId()));
        return dto;
    }

    @Override
    public Subject createSubject(SubjectDto dto, Long userId) {
        String trimmedName = dto.getName().trim();
        if (subjectRepository.existsByUserIdAndNameIgnoreCase(userId, trimmedName)) {
            throw new SubjectConflictException("A subject with the name '" + trimmedName + "' already exists in your account.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Subject subject = new Subject();
        subject.setName(trimmedName);
        subject.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        subject.setColor((dto.getColor() != null && !dto.getColor().isBlank()) ? dto.getColor() : "#4f46e5");
        subject.setUser(user);

        return subjectRepository.save(subject);
    }

    @Override
    public Subject updateSubject(Long id, SubjectDto dto, Long userId) {
        Subject subject = getSubjectById(id, userId);

        String trimmedName = dto.getName().trim();
        if (subjectRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, trimmedName, id)) {
            throw new SubjectConflictException("Another subject with the name '" + trimmedName + "' already exists.");
        }

        subject.setName(trimmedName);
        subject.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        if (dto.getColor() != null && !dto.getColor().isBlank()) {
            subject.setColor(dto.getColor());
        }

        return subjectRepository.save(subject);
    }

    @Override
    public void deleteSubject(Long id, Long userId) {
        Subject subject = getSubjectById(id, userId);

        long count = assignmentRepository.countBySubjectId(id);
        if (count > 0) {
            throw new SubjectConflictException("Cannot delete subject '" + subject.getName() +
                    "' because it contains " + count + " assignment(s). Please delete or reassign its assignments first to prevent accidental loss of academic records.");
        }

        subjectRepository.delete(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAssignmentsForSubject(Long subjectId) {
        return assignmentRepository.countBySubjectId(subjectId);
    }
}
