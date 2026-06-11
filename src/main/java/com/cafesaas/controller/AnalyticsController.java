package com.cafesaas.controller;

import com.cafesaas.dto.AnalyticsDto.*;
import com.cafesaas.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('MANAGER', 'TENANT_ADMIN')")
    public ResponseEntity<DashboardStats> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboard());
    }
}