package com.chocolateshop.dto;

import com.chocolateshop.entity.Enums;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for manual inventory stock adjustments (damage, physical recount, opening stock).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustmentDto {

    @NotNull(message = "Product is required")
    private Long productId;

    @NotNull(message = "Adjustment type is required")
    private Enums.TransactionType adjustmentType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    private String note;
}
