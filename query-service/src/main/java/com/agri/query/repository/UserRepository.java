package com.agri.query.repository;

import com.agri.query.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<User> findByEmail(String email);

    default java.util.Optional<User> findByUsername(String username) {
        return findByEmailIgnoreCase(username);
    }
}
