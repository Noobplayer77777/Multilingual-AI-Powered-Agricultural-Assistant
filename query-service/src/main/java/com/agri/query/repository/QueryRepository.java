package com.agri.query.repository;

import com.agri.query.entity.QueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueryRepository extends JpaRepository<QueryEntity, Integer> {
    List<QueryEntity> findAllByOrderByCreatedAtDesc();
    List<QueryEntity> findByUserIdOrderByCreatedAtDesc(Integer userId);
    Optional<QueryEntity> findByIdAndUserId(Integer id, Integer userId);
    void deleteByIdAndUserId(Integer id, Integer userId);
}
