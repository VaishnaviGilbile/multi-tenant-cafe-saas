package com.cafesaas.dto;

import com.cafesaas.security.Role;
import jakarta.validation.constraints.*;

public class StaffDto {

    public record CreateStaffRequest(
            @NotBlank                String name,
            @NotBlank @Email         String email,
            @NotBlank @Size(min = 8) String password,
            @NotNull                 Role   role,
            String                   phoneNumber
    ) {}
}