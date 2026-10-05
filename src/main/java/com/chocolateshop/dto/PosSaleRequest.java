package com.chocolateshop.dto;

import com.chocolateshop.entity.Enums;
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
 * Request payload for creating and completing a POS sale.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosSaleRequest {

    private Long customerId;

    @NotEmpty(message = "Cart cannot be empty")
    @Builder.Default
    private List<PosCartItem> items = new ArrayList<>();

    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @NotNull(message = "Paid amount is required")
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Builder.Default
    private Enums.PaymentMethod paymentMethod = Enums.PaymentMethod.CASH;

    @Builder.Default
    private List<PosPaymentLine> payments = new ArrayList<>();

    private String notes;
}
