package com.chocolateshop.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Payload for creating and recording stock purchase orders from suppliers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequest {

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotEmpty(message = "Purchase order must contain at least one item")
    @Builder.Default
    private List<PurchaseItemDto> items = new ArrayList<>();

    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    private String notes;
}
