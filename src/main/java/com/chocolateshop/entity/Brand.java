package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Chocolate manufacturer / brand entity (e.g. Lindt, Cadbury, Ferrero).
 */
@Entity
@Table(name = "brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand extends BaseEntity {

    @NotBlank(message = "Brand name is required")
    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Enums.Status status = Enums.Status.ACTIVE;
}
