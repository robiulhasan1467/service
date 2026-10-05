package com.chocolateshop.repository;

import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    List<AppUser> findByRole(Enums.Role role);
    List<AppUser> findByStatus(Enums.Status status);
}
