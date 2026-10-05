package com.chocolateshop.repository;

import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE " +
           "(:categoryId IS NULL OR e.category.id = :categoryId) AND " +
           "(:paymentMethod IS NULL OR e.paymentMethod = :paymentMethod) AND " +
           "(:startDate IS NULL OR e.expenseDate >= :startDate) AND " +
           "(:endDate IS NULL OR e.expenseDate <= :endDate)")
    Page<Expense> findWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("paymentMethod") Enums.PaymentMethod paymentMethod,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumExpensesBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e WHERE e.expenseDate BETWEEN :start AND :end GROUP BY e.category.name")
    List<Object[]> sumByCategoryBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
