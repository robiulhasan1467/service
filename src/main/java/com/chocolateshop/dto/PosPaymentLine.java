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
 * Split payment line for multiple tender types in a single checkout.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosPaymentLine {

    @NotNull(message = "Payment method is required")
    private Enums.PaymentMethod method;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    private BigDecimal amount;

    private String reference;
}
