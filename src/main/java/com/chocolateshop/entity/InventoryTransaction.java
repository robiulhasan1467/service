package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable inventory transaction ledger tracking stock movements.
 */
@Entity
@Table(name = "inventory_transactions", indexes = {
        @Index(name = "idx_inv_product_date", columnList = "product_id, transaction_date"),
        @Index(name = "idx_inv_type", columnList = "transaction_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction extends BaseEntity {

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private Enums.TransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @NotNull
    @Column(name = "previous_stock", nullable = false, precision = 12, scale = 2)
    private BigDecimal previousStock;

    @NotNull
    @Column(name = "new_stock", nullable = false, precision = 12, scale = 2)
    private BigDecimal newStock;

    @Column(length = 200)
    private String reference;

    @Column(name = "transaction_date", nullable = false)
    @Builder.Default
    private LocalDateTime transactionDate = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(columnDefinition = "TEXT")
    private String note;
}
