package com.agri.query.repository;

import com.agri.query.entity.FarmerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FarmerProfileRepository extends JpaRepository<FarmerProfile, Integer> {
    Optional<FarmerProfile> findByUserId(Integer userId);
}
