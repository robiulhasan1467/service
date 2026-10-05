package com.chocolateshop.repository;

import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Sale;
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
public interface SaleRepository extends JpaRepository<Sale, Long> {

    Optional<Sale> findByInvoiceNo(String invoiceNo);

    List<Sale> findByCustomerIdOrderBySaleDateDesc(Long customerId);

    @Query("SELECT s FROM Sale s WHERE s.customer.phone = :phone ORDER BY s.saleDate DESC")
    List<Sale> findByCustomerPhone(@Param("phone") String phone);

    List<Sale> findTop10ByOrderBySaleDateDesc();

    @Query("SELECT s FROM Sale s WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(s.invoiceNo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(s.customer.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.customer.phone) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:deliveryStatus IS NULL OR s.deliveryStatus = :deliveryStatus) AND " +
           "(cast(:startDate as timestamp) IS NULL OR s.saleDate >= :startDate) AND " +
           "(cast(:endDate as timestamp) IS NULL OR s.saleDate <= :endDate)")
    Page<Sale> findWithDeliveryFilters(
            @Param("query") String query,
            @Param("status") Enums.SaleStatus status,
            @Param("deliveryStatus") Enums.DeliveryStatus deliveryStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    default Page<Sale> findWithFilters(String query, Enums.SaleStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return findWithDeliveryFilters(query, status, null, startDate, endDate, pageable);
    }

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.status != 'CANCELLED' AND s.saleDate BETWEEN :start AND :end")
    BigDecimal sumTotalSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.status != 'CANCELLED' AND s.saleDate BETWEEN :start AND :end")
    long countOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.profit), 0) FROM Sale s WHERE s.status != 'CANCELLED' AND s.saleDate BETWEEN :start AND :end")
    BigDecimal sumProfitBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.paidAmount), 0) FROM Sale s WHERE s.status != 'CANCELLED' AND s.paymentMethod = 'CASH' AND s.saleDate BETWEEN :start AND :end")
    BigDecimal sumCashPaidBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.dueAmount), 0) FROM Sale s WHERE s.status != 'CANCELLED'")
    BigDecimal sumTotalDue();
}
