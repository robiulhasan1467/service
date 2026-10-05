package com.chocolateshop.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Payload for processing customer sale returns.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleReturnRequest {

    @NotNull(message = "Sale ID is required")
    private Long saleId;

    private String reason;

    @NotEmpty(message = "Return items cannot be empty")
    @Builder.Default
    private List<SaleReturnItemDto> items = new ArrayList<>();
}
