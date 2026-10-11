package com.agri.query.repository;

import com.agri.query.entity.SchemeAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchemeAssessmentRepository extends JpaRepository<SchemeAssessment, Integer> {
    List<SchemeAssessment> findByUserIdOrderByAssessedAtDesc(Integer userId);
    Optional<SchemeAssessment> findByIdAndUserId(Integer id, Integer userId);
    void deleteByIdAndUserId(Integer id, Integer userId);
}
