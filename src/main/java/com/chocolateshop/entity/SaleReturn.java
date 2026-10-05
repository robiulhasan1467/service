package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sales return transaction recording refunded items and inventory reversions.
 */
@Entity
@Table(name = "sales_returns", indexes = {
        @Index(name = "idx_return_no", columnList = "return_no"),
        @Index(name = "idx_return_date", columnList = "return_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleReturn extends BaseEntity {

    @NotBlank(message = "Return reference number is required")
    @Column(name = "return_no", nullable = false, unique = true, length = 100)
    private String returnNo;

    @NotNull(message = "Original sale is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Column(name = "return_date", nullable = false)
    @Builder.Default
    private LocalDateTime returnDate = LocalDateTime.now();

    @NotNull
    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_user_id")
    private AppUser processedBy;

    @OneToMany(mappedBy = "saleReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleReturnItem> items = new ArrayList<>();

    public void addItem(SaleReturnItem item) {
        items.add(item);
        item.setSaleReturn(this);
    }
}
