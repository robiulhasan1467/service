package com.chocolateshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Bangladeshi Live Shopping One-Click Quick Checkout ("অর্ডার করুন").
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuickOrderRequest {

    // For single item 1-click checkout
    private Long productId;

    @Builder.Default
    private Integer quantity = 1;

    // Optional list for multi-item cart checkout
    private List<CartItemRequest> items;

    @NotBlank(message = "আপনার নাম লিখুন (Your name is required)")
    private String customerName;

    @NotBlank(message = "মোবাইল নম্বর লিখুন (Valid 11-digit mobile is required)")
    private String customerPhone;

    @NotBlank(message = "সম্পূর্ণ ডেলিভারি ঠিকানা লিখুন (Full address is required)")
    private String deliveryAddress;

    // "DHAKA" (৳60) or "OUTSIDE" (৳120)
    @Builder.Default
    private String deliveryZone = "DHAKA";

    // "CASH" (Cash on Delivery) or "BKASH" or "NAGAD"
    @Builder.Default
    private String paymentMethod = "CASH";

    private String notes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemRequest {
        @NotNull
        private Long productId;
        @NotNull
        private BigDecimal quantity;
    }
}
