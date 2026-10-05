package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sale order entity representing POS transactions.
 */
@Entity
@Table(name = "sales", indexes = {
        @Index(name = "idx_sale_invoice", columnList = "invoice_no"),
        @Index(name = "idx_sale_date", columnList = "sale_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale extends BaseEntity {

    @NotBlank(message = "Invoice number is required")
    @Column(name = "invoice_no", nullable = false, unique = true, length = 100)
    private String invoiceNo;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "sale_date", nullable = false)
    @Builder.Default
    private LocalDateTime saleDate = LocalDateTime.now();

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "due_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal dueAmount = BigDecimal.ZERO;

    @Column(name = "change_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal changeAmount = BigDecimal.ZERO;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal profit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    @Builder.Default
    private Enums.PaymentMethod paymentMethod = Enums.PaymentMethod.CASH;

    @Column(name = "payment_details", length = 255)
    private String paymentDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Enums.SaleStatus status = Enums.SaleStatus.COMPLETED;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", length = 30)
    @Builder.Default
    private Enums.DeliveryStatus deliveryStatus = Enums.DeliveryStatus.PENDING;

    @Column(name = "courier_name", length = 100)
    private String courierName;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_user_id")
    private AppUser cashier;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalePayment> payments = new ArrayList<>();

    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }

    public void addPayment(SalePayment payment) {
        payments.add(payment);
        payment.setSale(this);
    }
}
