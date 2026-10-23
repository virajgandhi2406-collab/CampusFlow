package com.campusflow.service.impl;

import com.campusflow.dto.AssignmentDto;
import com.campusflow.dto.AssignmentFilterDto;
import com.campusflow.entity.*;
import com.campusflow.exception.AccessDeniedException;
import com.campusflow.exception.ResourceNotFoundException;
import com.campusflow.repository.AssignmentRepository;
import com.campusflow.repository.SubjectRepository;
import com.campusflow.repository.UserRepository;
import com.campusflow.service.AssignmentService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository,
                                 SubjectRepository subjectRepository,
                                 UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Assignment> getAssignments(Long userId, AssignmentFilterDto filter) {
        Specification<Assignment> spec = buildSpecification(userId, filter);
        Sort sort = determineSort(filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        return assignmentRepository.findAll(spec, pageable);
    }

    private Specification<Assignment> buildSpecification(Long userId, AssignmentFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // User data isolation: ensure assignments always belong to the authenticated user
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // Eagerly fetch subject for list display to eliminate N+1 queries
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("subject");
            }

            // Search by title
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String searchPattern = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), searchPattern));
            }

            // Filter by subject
            if (filter.getSubjectId() != null) {
                predicates.add(cb.equal(root.get("subject").get("id"), filter.getSubjectId()));
            }

            // Filter by status (including OVERDUE virtual status)
            if (filter.getStatus() != null && !filter.getStatus().equalsIgnoreCase("ALL")) {
                if (filter.getStatus().equalsIgnoreCase("OVERDUE")) {
                    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                    predicates.add(cb.notEqual(root.get("status"), AssignmentStatus.COMPLETED));
                    predicates.add(cb.lessThan(root.get("dueDate"), now));
                } else {
                    try {
                        AssignmentStatus statusEnum = AssignmentStatus.valueOf(filter.getStatus().toUpperCase());
                        predicates.add(cb.equal(root.get("status"), statusEnum));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            // Filter by priority
            if (filter.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort determineSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.ASC, "dueDate");
        }
        return switch (sortBy) {
            case "dueDateDesc" -> Sort.by(Sort.Direction.DESC, "dueDate");
            case "priorityDesc" -> Sort.by(Sort.Direction.DESC, "priority").and(Sort.by(Sort.Direction.ASC, "dueDate"));
            case "priorityAsc" -> Sort.by(Sort.Direction.ASC, "priority").and(Sort.by(Sort.Direction.ASC, "dueDate"));
            case "titleAsc" -> Sort.by(Sort.Direction.ASC, "title");
            default -> Sort.by(Sort.Direction.ASC, "dueDate");
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Assignment getAssignmentById(Long id, Long userId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + id));

        if (!assignment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to access this assignment");
        }
        return assignment;
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentDto getAssignmentDtoById(Long id, Long userId) {
        Assignment assignment = getAssignmentById(id, userId);
        AssignmentDto dto = new AssignmentDto();
        dto.setId(assignment.getId());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setSubjectId(assignment.getSubject().getId());
        dto.setSubjectName(assignment.getSubject().getName());
        dto.setSubjectColor(assignment.getSubject().getColor());
        dto.setDueDateTime(assignment.getDueDate().toLocalDateTime());
        dto.setPriority(assignment.getPriority());
        dto.setStatus(assignment.getStatus());
        return dto;
    }

    @Override
    public Assignment createAssignment(AssignmentDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Enforce subject ownership: User can only assign to their own subject
        Subject subject = subjectRepository.findByIdAndUserId(dto.getSubjectId(), userId)
                .orElseThrow(() -> new AccessDeniedException("Selected subject does not exist or does not belong to you"));

        OffsetDateTime dueDateTimeUtc = dto.getDueDateTime().atOffset(ZoneOffset.UTC);

        Assignment assignment = new Assignment(
                dto.getTitle().trim(),
                dto.getDescription() != null ? dto.getDescription().trim() : null,
                subject,
                user,
                dueDateTimeUtc,
                dto.getPriority(),
                dto.getStatus()
        );

        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment updateAssignment(Long id, AssignmentDto dto, Long userId) {
        Assignment assignment = getAssignmentById(id, userId);

        // Enforce subject ownership on update
        Subject subject = subjectRepository.findByIdAndUserId(dto.getSubjectId(), userId)
                .orElseThrow(() -> new AccessDeniedException("Selected subject does not exist or does not belong to you"));

        assignment.setTitle(dto.getTitle().trim());
        assignment.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        assignment.setSubject(subject);
        assignment.setDueDate(dto.getDueDateTime().atOffset(ZoneOffset.UTC));
        assignment.setPriority(dto.getPriority());
        assignment.setStatus(dto.getStatus());

        return assignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignment(Long id, Long userId) {
        Assignment assignment = getAssignmentById(id, userId);
        assignmentRepository.delete(assignment);
    }

    @Override
    public Assignment toggleAssignmentCompletion(Long id, Long userId) {
        Assignment assignment = getAssignmentById(id, userId);
        if (assignment.getStatus() == AssignmentStatus.COMPLETED) {
            assignment.markPending();
        } else {
            assignment.markComplete();
        }
        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment updateAssignmentStatus(Long id, AssignmentStatus status, Long userId) {
        Assignment assignment = getAssignmentById(id, userId);
        assignment.setStatus(status);
        return assignmentRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsForSubject(Long subjectId, Long userId) {
        // Verify subject ownership first
        subjectRepository.findByIdAndUserId(subjectId, userId)
                .orElseThrow(() -> new AccessDeniedException("Subject not found or access denied"));
        return assignmentRepository.findByUserIdAndSubjectIdOrderByDueDateAsc(userId, subjectId);
    }
}
