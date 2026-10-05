package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment received from a customer settling previous dues.
 */
@Entity
@Table(name = "customer_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPayment extends BaseEntity {

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull(message = "Amount is required")
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
    @JoinColumn(name = "received_by_user_id")
    private AppUser receivedBy;
}
