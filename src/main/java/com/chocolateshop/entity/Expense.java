package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Operating expense entry (e.g. rent, electricity, packaging, marketing).
 */
@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_date", columnList = "expense_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense extends BaseEntity {

    @NotNull(message = "Expense category is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private ExpenseCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Expense amount must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Expense date is required")
    @Column(name = "expense_date", nullable = false)
    @Builder.Default
    private LocalDate expenseDate = LocalDate.now();

    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    @Builder.Default
    private Enums.PaymentMethod paymentMethod = Enums.PaymentMethod.CASH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_user_id")
    private AppUser recordedBy;
}
