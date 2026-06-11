package com.cafesaas.controller;

import com.cafesaas.domain.*;
import com.cafesaas.dto.MenuDto.*;
import com.cafesaas.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @PreAuthorize runs AFTER authentication but BEFORE the method body.
 * It evaluates a SpEL expression against the security context.
 * hasAnyRole('MANAGER') matches "ROLE_MANAGER" in the authority list
 * (Spring automatically prepends "ROLE_" when using hasRole/hasAnyRole).
 */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // Anyone authenticated can view the menu (staff, cashier, manager, admin)
    @GetMapping
    public ResponseEntity<List<MenuItem>> getMenu(
            @RequestParam(required = false) MenuCategory category) {
        List<MenuItem> items = (category != null)
                ? menuService.getByCategory(category)
                : menuService.getActiveMenu();
        return ResponseEntity.ok(items);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<MenuItem> create(@Valid @RequestBody CreateMenuItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<MenuItem> update(
            @PathVariable String id,
            @Valid @RequestBody CreateMenuItemRequest req) {
        return ResponseEntity.ok(menuService.update(id, req));
    }

    // PATCH for a partial update — toggling one field
    @PatchMapping("/{id}/toggle-availability")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<MenuItem> toggleAvailability(@PathVariable String id) {
        return ResponseEntity.ok(menuService.toggleAvailability(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();   // 204 — success with no body
    }
}