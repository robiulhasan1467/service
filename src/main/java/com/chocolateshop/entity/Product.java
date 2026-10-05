package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Product / Chocolate entity representing store items.
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_sku", columnList = "sku"),
        @Index(name = "idx_product_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @NotBlank(message = "Product code/SKU is required")
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false, length = 200)
    private String name;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", message = "Purchase price cannot be negative")
    @Column(name = "purchase_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "discount_price", precision = 12, scale = 2)
    private BigDecimal discountPrice;

    @NotNull(message = "Stock quantity is required")
    @Column(name = "stock_quantity", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal stockQuantity = BigDecimal.ZERO;

    @NotNull(message = "Minimum stock level is required")
    @Column(name = "min_stock_level", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minStockLevel = new BigDecimal("5.00");

    @Column(length = 50)
    @Builder.Default
    private String unit = "pcs";

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Enums.Status status = Enums.Status.ACTIVE;

    public boolean isLowStock() {
        return stockQuantity != null && minStockLevel != null &&
                stockQuantity.compareTo(minStockLevel) <= 0;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon() {
        return expiryDate != null && !isExpired() &&
                expiryDate.isBefore(LocalDate.now().plusDays(30));
    }

    public BigDecimal getEffectivePrice() {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0
                && discountPrice.compareTo(sellingPrice) < 0) {
            return discountPrice;
        }
        return sellingPrice;
    }
}
