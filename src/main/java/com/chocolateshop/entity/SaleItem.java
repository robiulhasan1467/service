package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Line item in a customer sale order.
 */
@Entity
@Table(name = "sale_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "returned_quantity", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal returnedQuantity = BigDecimal.ZERO;

    @NotNull
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @Column(name = "purchase_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchaseCost;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal profit = BigDecimal.ZERO;

    public BigDecimal getAvailableToReturn() {
        BigDecimal returned = (returnedQuantity != null) ? returnedQuantity : BigDecimal.ZERO;
        return quantity.subtract(returned);
    }
}
