package com.chocolateshop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Line item in a sales return request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleReturnItemDto {

    @NotNull(message = "Sale item ID is required")
    private Long saleItemId;

    @NotNull(message = "Return quantity is required")
    @DecimalMin(value = "0.01", message = "Return quantity must be greater than zero")
    private BigDecimal returnQuantity;
}
