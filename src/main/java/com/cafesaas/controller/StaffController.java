package com.cafesaas.controller;

import com.cafesaas.domain.Staff;
import com.cafesaas.dto.StaffDto.*;
import com.cafesaas.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'TENANT_ADMIN')")
    public ResponseEntity<List<Staff>> getAll() {
        return ResponseEntity.ok(staffService.getAllActive());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TENANT_ADMIN')")
    public ResponseEntity<Staff> getById(@PathVariable String id) {
        return ResponseEntity.ok(staffService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Staff> create(@Valid @RequestBody CreateStaffRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(staffService.create(req));
    }

    // DELETE maps to soft-deactivate — not a hard DB delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        staffService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}