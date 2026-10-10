package com.agri.query.repository;

import com.agri.query.entity.ResponseEntityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResponseRepository extends JpaRepository<ResponseEntityData, Integer> {
    Optional<ResponseEntityData> findByQueryId(Integer queryId);
}
