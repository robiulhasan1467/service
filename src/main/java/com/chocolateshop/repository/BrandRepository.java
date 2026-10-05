package com.chocolateshop.repository;

import com.chocolateshop.entity.Brand;
import com.chocolateshop.entity.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    List<Brand> findByStatusOrderByNameAsc(Enums.Status status);
}
