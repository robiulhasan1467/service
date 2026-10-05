package com.chocolateshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * System user entity for authentication and role-based authorization.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser extends BaseEntity {

    @NotBlank(message = "Username is required")
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 150)
    private String email;

    @Column(length = 50)
    private String phone;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Enums.Role role;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Enums.Status status = Enums.Status.ACTIVE;
}
