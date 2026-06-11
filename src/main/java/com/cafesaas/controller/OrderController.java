package com.cafesaas.controller;


import com.cafesaas.domain.*;
import com.cafesaas.dto.OrderDto.*;
import com.cafesaas.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'CASHIER', 'MANAGER', 'TENANT_ADMIN')")
    public ResponseEntity<Order> create(@Valid @RequestBody CreateOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(req));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'TENANT_ADMIN')")
    public ResponseEntity<List<Order>> getActive() {
        return ResponseEntity.ok(orderService.getActiveOrders());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'TENANT_ADMIN')")
    public ResponseEntity<Order> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest req) {
        return ResponseEntity.ok(orderService.updateStatus(id, req.status()));
    }

    // History is manager/admin only — staff don't need to see past orders
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('MANAGER', 'TENANT_ADMIN')")
    public ResponseEntity<Page<Order>> getHistory(
            @RequestParam Instant start,
            @RequestParam Instant end,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                orderService.getHistory(start, end, PageRequest.of(page, size)));
    }
}