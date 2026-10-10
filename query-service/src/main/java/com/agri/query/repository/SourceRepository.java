package com.agri.query.repository;

import com.agri.query.entity.SourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceRepository extends JpaRepository<SourceEntity, Long> {
    List<SourceEntity> findByResponseId(Long responseId);
}
