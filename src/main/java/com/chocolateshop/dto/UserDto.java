package com.chocolateshop.dto;

import com.chocolateshop.entity.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for employee/user management forms.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String email;

    private String phone;

    @NotNull(message = "Role is required")
    private Enums.Role role;

    @NotNull(message = "Status is required")
    @Builder.Default
    private Enums.Status status = Enums.Status.ACTIVE;
}
