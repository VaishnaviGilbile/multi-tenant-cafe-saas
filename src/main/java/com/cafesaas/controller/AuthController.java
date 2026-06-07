package com.cafesaas.controller;

import com.cafesaas.dto.AuthDto.*;
import com.cafesaas.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-tenant")
    public ResponseEntity<LoginResponse> registerTenant(
            @Valid @RequestBody RegisterTenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerTenant(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}