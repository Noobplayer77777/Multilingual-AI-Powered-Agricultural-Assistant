package com.agri.query.repository;

import com.agri.query.entity.QueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryRepository extends JpaRepository<QueryEntity, Long> {
    List<QueryEntity> findAllByOrderByCreatedAtDesc();
}
