package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment transaction made to a supplier against purchases / dues.
 */
@Entity
@Table(name = "supplier_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierPayment extends BaseEntity {

    @NotNull(message = "Supplier is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private Enums.PaymentMethod paymentMethod;

    @Column(name = "payment_date", nullable = false)
    @Builder.Default
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Column(length = 100)
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_user_id")
    private AppUser paidBy;
}
