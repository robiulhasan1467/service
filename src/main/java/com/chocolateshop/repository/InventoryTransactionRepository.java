package com.chocolateshop.repository;

import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByProductIdOrderByTransactionDateDesc(Long productId);

    @Query("SELECT it FROM InventoryTransaction it WHERE " +
           "(:productId IS NULL OR it.product.id = :productId) AND " +
           "(:type IS NULL OR it.transactionType = :type) AND " +
           "(:startDate IS NULL OR it.transactionDate >= :startDate) AND " +
           "(:endDate IS NULL OR it.transactionDate <= :endDate) " +
           "ORDER BY it.transactionDate DESC")
    Page<InventoryTransaction> findWithFilters(
            @Param("productId") Long productId,
            @Param("type") Enums.TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
