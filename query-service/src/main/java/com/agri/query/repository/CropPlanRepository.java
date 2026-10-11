package com.agri.query.repository;

import com.agri.query.entity.CropPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropPlanRepository extends JpaRepository<CropPlan, Integer> {
    List<CropPlan> findByUserIdOrderByCreatedAtDesc(Integer userId);
    Optional<CropPlan> findByIdAndUserId(Integer id, Integer userId);
    void deleteByIdAndUserId(Integer id, Integer userId);
}
