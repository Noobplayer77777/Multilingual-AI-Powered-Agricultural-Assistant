package com.agri.query.repository;

import com.agri.query.entity.ResponseEntityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseRepository extends JpaRepository<ResponseEntityData, Long> {
}
