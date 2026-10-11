package com.agri.query.repository;

import com.agri.query.entity.FarmReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmReminderRepository extends JpaRepository<FarmReminder, Integer> {
    List<FarmReminder> findByUserIdOrderByDueDateAsc(Integer userId);
    Optional<FarmReminder> findByIdAndUserId(Integer id, Integer userId);
    void deleteByIdAndUserId(Integer id, Integer userId);
}
