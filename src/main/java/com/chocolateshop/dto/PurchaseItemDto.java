package com.chocolateshop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Line item in a purchase order request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseItemDto {

    @NotNull(message = "Product is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.00", message = "Purchase price cannot be negative")
    private BigDecimal unitPrice;
}
