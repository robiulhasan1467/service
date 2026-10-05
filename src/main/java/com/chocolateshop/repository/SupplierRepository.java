package com.chocolateshop.repository;

import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByStatusOrderByNameAsc(Enums.Status status);

    @Query("SELECT s FROM Supplier s WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(s.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(s.phone) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Supplier> searchSuppliers(@Param("query") String query);
}
