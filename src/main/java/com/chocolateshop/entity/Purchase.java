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
 * Purchase order entity for inventory replenishment from suppliers.
 */
@Entity
@Table(name = "purchases", indexes = {
        @Index(name = "idx_purchase_invoice", columnList = "invoice_no"),
        @Index(name = "idx_purchase_date", columnList = "purchase_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase extends BaseEntity {

    @NotBlank(message = "Invoice number is required")
    @Column(name = "invoice_no", nullable = false, unique = true, length = 100)
    private String invoiceNo;

    @NotNull(message = "Supplier is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "purchase_date", nullable = false)
    @Builder.Default
    private LocalDateTime purchaseDate = LocalDateTime.now();

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Enums.PurchaseStatus status = Enums.PurchaseStatus.CONFIRMED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id")
    private AppUser creator;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseItem> items = new ArrayList<>();

    public void addItem(PurchaseItem item) {
        items.add(item);
        item.setPurchase(this);
    }
}
