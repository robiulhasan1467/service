package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Breakdown of payments applied to a sale transaction (supporting split/mixed payments).
 */
@Entity
@Table(name = "sale_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalePayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private Enums.PaymentMethod paymentMethod;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;
}
