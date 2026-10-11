package com.agri.query.repository;

import com.agri.query.entity.FarmPlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmPlotRepository extends JpaRepository<FarmPlot, Integer> {
    List<FarmPlot> findByUserIdOrderByPlotNameAsc(Integer userId);
    Optional<FarmPlot> findByIdAndUserId(Integer id, Integer userId);
    void deleteByIdAndUserId(Integer id, Integer userId);
}
