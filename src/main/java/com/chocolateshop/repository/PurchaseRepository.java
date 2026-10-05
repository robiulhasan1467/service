package com.chocolateshop.repository;

import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    Optional<Purchase> findByInvoiceNo(String invoiceNo);

    List<Purchase> findBySupplierIdOrderByPurchaseDateDesc(Long supplierId);

    @Query("SELECT p FROM Purchase p WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(p.invoiceNo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.supplier.name) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:startDate IS NULL OR p.purchaseDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.purchaseDate <= :endDate)")
    Page<Purchase> findWithFilters(
            @Param("query") String query,
            @Param("status") Enums.PurchaseStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Purchase p WHERE p.status = 'CONFIRMED' AND p.purchaseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(p.dueAmount), 0) FROM Purchase p WHERE p.status = 'CONFIRMED'")
    BigDecimal sumTotalDue();
}
