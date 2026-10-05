package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Individual product item returned in a sales return.
 */
@Entity
@Table(name = "sales_return_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleReturnItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_return_id", nullable = false)
    private SaleReturn saleReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_item_id", nullable = false)
    private SaleItem saleItem;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Returned quantity must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @NotNull
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @Column(name = "refund_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundTotal;

    @NotNull
    @Column(name = "reversed_profit", nullable = false, precision = 12, scale = 2)
    private BigDecimal reversedProfit;
}
