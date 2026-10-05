package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

/**
 * Supplier entity providing chocolate products and ingredients.
 */
@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends BaseEntity {

    @NotBlank(message = "Supplier name is required")
    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(length = 50)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "opening_due", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal openingDue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Enums.Status status = Enums.Status.ACTIVE;
}
