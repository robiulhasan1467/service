package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

/**
 * Customer entity for both registered clients and walk-in shoppers.
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @NotBlank(message = "Customer name is required")
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "opening_due", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal openingDue = BigDecimal.ZERO;

    @Column(name = "is_walk_in")
    @Builder.Default
    private Boolean isWalkIn = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Enums.Status status = Enums.Status.ACTIVE;
}
