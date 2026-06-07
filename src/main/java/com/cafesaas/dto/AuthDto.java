package com.cafesaas.dto;

import jakarta.validation.constraints.*;

public class AuthDto {

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password
    ) {}

    public record LoginResponse(
            String token,
            String userId,
            String tenantId,
            String role
    ) {}

    public record RegisterTenantRequest(
            @NotBlank @Size(min = 3, max = 100) String tenantId,
            @NotBlank                            String tenantName,
            @NotBlank @Size(min = 3, max = 50)  String subdomain,
            @NotBlank                            String adminName,
            @NotBlank @Email                     String adminEmail,
            @NotBlank @Size(min = 8)            String adminPassword
    ) {}
}