package com.campusflow.repository;

import com.campusflow.entity.Assignment;
import com.campusflow.entity.AssignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long>, JpaSpecificationExecutor<Assignment> {

    Optional<Assignment> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.subject s WHERE a.id = :id AND a.user.id = :userId")
    Optional<Assignment> findByIdAndUserIdWithSubject(@Param("id") Long id, @Param("userId") Long userId);

    List<Assignment> findByUserIdAndSubjectIdOrderByDueDateAsc(Long userId, Long subjectId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, AssignmentStatus status);

    long countByUserIdAndStatusNotAndDueDateBefore(Long userId, AssignmentStatus status, OffsetDateTime dateTime);

    long countByUserIdAndStatusNotAndDueDateBetween(Long userId, AssignmentStatus status, OffsetDateTime start, OffsetDateTime end);

    long countBySubjectId(Long subjectId);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.subject s WHERE a.user.id = :userId AND a.status != :status ORDER BY a.dueDate ASC LIMIT 5")
    List<Assignment> findUpcomingAssignments(@Param("userId") Long userId, @Param("status") AssignmentStatus status);

    List<Assignment> findByUserIdAndCompletedAtBetween(Long userId, OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.subject s WHERE a.user.id = :userId AND a.status != 'COMPLETED' AND a.dueDate < :now ORDER BY a.dueDate ASC")
    List<Assignment> findOverdueAssignments(@Param("userId") Long userId, @Param("now") OffsetDateTime now);
}
