package com.campusflow.repository;

import com.campusflow.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByUserIdOrderByNameAsc(Long userId);

    Optional<Subject> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);

    boolean existsByUserIdAndNameIgnoreCaseAndIdNot(Long userId, String name, Long id);

    long countByUserId(Long userId);

    @Query("SELECT s FROM Subject s LEFT JOIN FETCH s.assignments a WHERE s.id = :id AND s.user.id = :userId")
    Optional<Subject> findByIdAndUserIdWithAssignments(@Param("id") Long id, @Param("userId") Long userId);
}
