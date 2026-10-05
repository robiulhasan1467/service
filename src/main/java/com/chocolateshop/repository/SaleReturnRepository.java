package com.chocolateshop.repository;

import com.chocolateshop.entity.SaleReturn;
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
public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {

    Optional<SaleReturn> findByReturnNo(String returnNo);

    List<SaleReturn> findBySaleId(Long saleId);

    @Query("SELECT r FROM SaleReturn r WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(r.returnNo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(r.sale.invoiceNo) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(cast(:startDate as timestamp) IS NULL OR r.returnDate >= :startDate) AND " +
           "(cast(:endDate as timestamp) IS NULL OR r.returnDate <= :endDate)")
    Page<SaleReturn> findWithFilters(
            @Param("query") String query,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM SaleReturn r WHERE r.returnDate BETWEEN :start AND :end")
    BigDecimal sumRefundsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
