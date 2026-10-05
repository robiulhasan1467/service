package com.chocolateshop.repository;

import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.SalePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalePaymentRepository extends JpaRepository<SalePayment, Long> {

    List<SalePayment> findBySaleId(Long saleId);

    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM SalePayment p " +
           "WHERE p.sale.status != 'CANCELLED' AND p.sale.saleDate BETWEEN :start AND :end " +
           "GROUP BY p.paymentMethod")
    List<Object[]> sumByPaymentMethodBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM SalePayment p " +
           "WHERE p.paymentMethod = :method AND p.sale.status != 'CANCELLED' AND p.sale.saleDate BETWEEN :start AND :end")
    BigDecimal sumAmountByMethodBetween(@Param("method") Enums.PaymentMethod method,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);
}
